# Tài liệu kiến trúc & cấu trúc dự án

> **Stack:** Java 21 · Spring Boot 4 · PostgreSQL · Redis (optional) · JWT · VNPay · MapStruct · Lombok

---

## Mục lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Cấu trúc thư mục](#2-cấu-trúc-thư-mục)
3. [Module Config](#3-module-config)
4. [Module Entity (Thực thể dữ liệu)](#4-module-entity)
5. [Module Repository](#5-module-repository)
6. [Module Service (Nghiệp vụ)](#6-module-service)
7. [Module Controller (API endpoints)](#7-module-controller)
8. [Module DTO](#8-module-dto)
9. [Module Mapper](#9-module-mapper)
10. [Module Exception](#10-module-exception)
11. [Module Enums](#11-module-enums)
12. [Module Validator](#12-module-validator)
13. [Module Messaging (Redis)](#13-module-messaging-redis)
14. [Luồng xử lý chính](#14-luồng-xử-lý-chính)
15. [Bảng API endpoints](#15-bảng-api-endpoints)
16. [Cấu hình môi trường](#16-cấu-hình-môi-trường)

---

## 1. Tổng quan kiến trúc

```
Client (Postman / Frontend)
        │
        ▼
  [Controller Layer]       ← Nhận HTTP request, trả HTTP response
        │
        ▼
  [Service Layer]          ← Xử lý business logic
        │
  ┌─────┴──────┐
  ▼            ▼
[Repository] [External]   ← JPA truy DB / VNPay API / Redis
  │
  ▼
[PostgreSQL DB]
```

**Luồng bảo mật JWT:**
```
Request → SecurityFilterChain → CustomJwtDecoder → JwtAuthenticationConverter → Controller
```

---

## 2. Cấu trúc thư mục

```
src/main/java/com/example/
│
├── javaspringboot/                  # Entry point + entity + repository
│   ├── JavaspringbootApplication.java
│   ├── entity/                      # JPA Entities (bảng DB)
│   └── repository/                  # Spring Data JPA Repositories
│
├── config/                          # Cấu hình Spring (Security, JWT, VNPay, Redis...)
├── controller/                      # REST Controllers (API endpoints)
├── Service/                         # Business logic
├── dto/                             # Data Transfer Objects
│   ├── request/                     # Request body / ApiResponse wrapper
│   ├── response/                    # Response body
│   └── vnpay/                       # VNPay-specific DTOs
├── mapper/                          # MapStruct interface (Entity ↔ DTO)
├── exception/                       # Custom exception + global handler
├── enums/                           # Enum constants
├── messaging/                       # Redis Pub/Sub
└── Validator/                       # Custom validation annotations
```

---

## 3. Module Config

| File | Vai trò |
|------|---------|
| `SecurityConfig.java` | Cấu hình Spring Security: định nghĩa endpoint public/private, JWT OAuth2 resource server, BCrypt password encoder |
| `CustomJwtDecoder.java` | Custom decoder kiểm tra token có bị blacklist (trong bảng `invalidated_token`) không trước khi decode |
| `JwtAuthencationEntryPoint.java` | Trả về lỗi `401 Unauthorized` dạng JSON thay vì redirect khi token không hợp lệ |
| `ApplicationInitConfig.java` | Tự động tạo user `admin` khi khởi động lần đầu nếu chưa tồn tại |
| `VNPayConfig.java` | Chứa credentials VNPay (tmnCode, hashSecret, URL...) đọc từ `application-dev.properties`. Cũng định nghĩa `RestTemplate` bean |
| `RedisConfig.java` | Cấu hình Redis connection, serializer, pub/sub listener (chỉ khởi động khi `redis.enabled=true`) |
| `OpenApiConfig.java` | Cấu hình Swagger UI / SpringDoc (thêm JWT Bearer auth vào Swagger) |

**Endpoint public (không cần JWT):**
```
POST  /users               → đăng ký tài khoản
POST  /auth/token          → đăng nhập
POST  /auth/introspect     → kiểm tra token
POST  /auth/logout         → đăng xuất
POST  /auth/refresh        → làm mới token
GET   /products            → danh sách sản phẩm (public)
GET   /products/**         → chi tiết sản phẩm (public)
GET   /categories          → danh sách danh mục (public)
GET   /payments/vnpay/ipn  → IPN callback từ VNPay server
GET   /payments/vnpay/return → redirect sau thanh toán
      /swagger-ui/**       → Swagger UI
```

---

## 4. Module Entity

Các class ánh xạ với bảng PostgreSQL (JPA `@Entity`).

### `User`
```
users (id, username, email, password, firstName, lastName, dob)
  └── user_roles (user_id, roles)   ← @ElementCollection, Set<String>
```
- Lưu roles dạng Set<String> (`ADMIN`, `USER`) trong bảng phụ `user_roles`
- Password được hash bằng **BCrypt** trước khi lưu

### `Product`
```
products (id, name, description, price, stock, category)
```
- `stock`: số lượng tồn kho, tự động giảm khi đặt hàng

### `Category`
```
categories (id, name, description)
```

### `CartItem`
```
cart_items (id, user_id, product_id, quantity)
```
- Giỏ hàng của user: mỗi dòng là 1 sản phẩm với số lượng

### `Order`
```
orders (id, user_id, totalPrice, status, createdAt)
  └── order_items (id, order_id, product_id, quantity, priceAtPurchase)
```
- `status`: `PENDING` → `COMPLETED` | `CANCELLED`
- `priceAtPurchase`: giá tại thời điểm mua (không thay đổi dù sản phẩm đổi giá sau)

### `Payment`
```
payments (id, order_id, amount, status, paymentMethod,
          requestId, transactionId, payUrl[TEXT],
          qrCodeUrl, deeplink, createdAt, paidAt)
```
- `payUrl` kiểu `TEXT` (VNPay URL dài >255 ký tự)
- `paymentMethod`: `VNPAY` | `DEMO`
- `status`: `PENDING` → `SUCCESS` | `FAILED`

### `InvalidatedToken`
```
invalidated_token (id, expiryTime)
```
- Lưu JWT `jti` đã logout; `CustomJwtDecoder` kiểm tra blacklist tại mỗi request

---

## 5. Module Repository

Tất cả extend `JpaRepository<Entity, String>` — Spring Data JPA tự tạo implementation.

| Repository | Custom methods |
|------------|---------------|
| `UserRepository` | `findByUsername(String)` |
| `ProductRepository` | _(chỉ CRUD cơ bản)_ |
| `CategoryRepository` | _(chỉ CRUD cơ bản)_ |
| `CartItemRepository` | `findByUser(User)`, `findByUserAndProductId(User, String)`, `deleteByUser(User)` |
| `OrderRepository` | `findByUserOrderByCreatedAtDesc(User)` |
| `PaymentRepository` | `findByOrder(Order)`, `findByRequestId(String)` |
| `InvalidatedRepository` | `existsById(String)` |

---

## 6. Module Service

Toàn bộ business logic nằm ở đây. Mỗi service tương ứng 1 domain.

### `AuthenticationService`
- **`authenticate()`**: kiểm tra username/password → tạo JWT access token + refresh token
- **`introspect()`**: verify token còn hợp lệ không
- **`logout()`**: thêm `jti` vào blacklist (`invalidated_token`)
- **`refreshToken()`**: verify refresh token → cấp access token mới
- JWT được ký bằng **HMAC-SHA512**, chứa claims: `sub` (username), `scope` (roles), `jti`, `exp`

### `UserService`
- CRUD người dùng
- `getCurrentUser()`: lấy thông tin user đang đăng nhập từ SecurityContext
- Password được encode bằng BCrypt trước khi lưu

### `ProductService`
- CRUD sản phẩm (tạo/sửa/xóa yêu cầu role `ADMIN`)
- Dùng `ProductMapper` để convert Entity ↔ DTO

### `CategoryService`
- CRUD danh mục (tạo/sửa/xóa yêu cầu role `ADMIN`)

### `CartService`
- **`getCart()`**: lấy giỏ hàng của user hiện tại
- **`addToCart()`**: thêm sản phẩm (nếu đã có thì cộng thêm số lượng)
- **`updateCartItem()`**: cập nhật số lượng
- **`removeCartItem()`**: xóa 1 item khỏi giỏ

### `OrderService`
- **`buyNow()`**: đặt hàng ngay 1 sản phẩm (không qua giỏ), trừ stock
- **`checkout()`**: đặt hàng từ toàn bộ giỏ hàng, xóa giỏ sau khi tạo order
- **`getMyOrders()`**: danh sách đơn của user hiện tại
- **`getAllOrders()`**: toàn bộ đơn hàng (chỉ `ADMIN`)
- **`cancelOrder()`**: hủy đơn `PENDING`, hoàn trả stock
- **`updateOrderStatus()`**: admin cập nhật trạng thái đơn

### `PaymentService`
- **`createVNPayPayment()`**: kiểm tra order → gọi `VNPayService` tạo URL → lưu `Payment` PENDING → trả về `payUrl`
- **`handleVNPayIpn()`**: nhận IPN từ VNPay → verify signature → cập nhật `Payment` và `Order`
- **`completeDemoPayment()`**: đánh dấu thanh toán thành công ngay (dùng để test, không gọi VNPay)

### `VNPayService`
- **`createPaymentUrl()`**: build URL redirect đến VNPay với đầy đủ params và chữ ký HMAC-SHA512
- **`verifySignature()`**: xác minh signature từ IPN/return callback
- Thuật toán ký: sort params theo alphabet → build query string → HMAC-SHA512

---

## 7. Module Controller

Mỗi controller map 1 nhóm endpoint. Tất cả trả về dạng `ApiResponse<T>`.

### `AuthenticationController` — `/auth`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/auth/token` | Đăng nhập, trả JWT |
| POST | `/auth/introspect` | Kiểm tra token hợp lệ |
| POST | `/auth/logout` | Đăng xuất (blacklist token) |
| POST | `/auth/refresh` | Làm mới access token |

### `UserController` — `/users`
| Method | Endpoint | Auth |
|--------|----------|------|
| POST | `/users` | Public — đăng ký |
| GET | `/users` | ADMIN |
| GET | `/users/{id}` | ADMIN |
| GET | `/users/myInfo` | User |
| PUT | `/users/{id}` | User/ADMIN |
| DELETE | `/users/{id}` | ADMIN |

### `ProductController` — `/products`
| Method | Endpoint | Auth |
|--------|----------|------|
| GET | `/products` | Public |
| GET | `/products/{id}` | Public |
| POST | `/products` | ADMIN |
| PUT | `/products/{id}` | ADMIN |
| DELETE | `/products/{id}` | ADMIN |

### `CategoryController` — `/categories`
| Method | Endpoint | Auth |
|--------|----------|------|
| GET | `/categories` | Public |
| POST | `/categories` | ADMIN |
| PUT | `/categories/{id}` | ADMIN |
| DELETE | `/categories/{id}` | ADMIN |

### `CartController` — `/cart`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/cart` | Xem giỏ hàng |
| POST | `/cart` | Thêm sản phẩm |
| PUT | `/cart/{id}` | Cập nhật số lượng |
| DELETE | `/cart/{id}` | Xóa item |

### `OrderController` — `/orders`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/orders/buy-now` | Mua ngay (không qua giỏ) |
| POST | `/orders` | Checkout từ giỏ hàng |
| GET | `/orders/my` | Đơn hàng của tôi |
| GET | `/orders` | Tất cả đơn (ADMIN) |
| PUT | `/orders/{id}/cancel` | Hủy đơn |
| PUT | `/orders/{id}/status` | Đổi trạng thái (ADMIN) |

### `PaymentController` — `/payments`
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/payments/orders/{id}/vnpay` | Tạo URL thanh toán VNPay |
| POST | `/payments/orders/{id}/demo` | Thanh toán demo (test) |
| GET | `/payments/vnpay/ipn` | IPN callback từ VNPay (public) |
| GET | `/payments/vnpay/return` | Return URL sau thanh toán (public) |

### `MessagingController` — `/api/messages`
- Chỉ khởi động khi `redis.enabled=true`
- Publish message lên Redis channel để test Pub/Sub

---

## 8. Module DTO

### Request DTOs
| Class | Dùng cho |
|-------|---------|
| `UserCreationRequest` | Đăng ký user (username, password, email, dob...) |
| `UserUpdateRequest` | Cập nhật thông tin user |
| `AuthenticationRequest` | Đăng nhập (username, password) |
| `IntrospectRequest` | Kiểm tra token |
| `LogoutRequest` | Đăng xuất (token) |
| `RefreshRequest` | Refresh token |
| `ProductRequest` | Tạo/sửa sản phẩm |
| `CategoryRequest` | Tạo/sửa danh mục |
| `CartItemRequest` | Thêm vào giỏ / mua ngay (productId, quantity) |
| `MessageRequest` | Publish Redis message |
| `ApiResponse<T>` | **Wrapper chung** cho mọi response: `{code, message, result}` |

### Response DTOs
| Class | Nội dung |
|-------|---------|
| `UserResponse` | Thông tin user (không có password) |
| `AuthenticationResponse` | `{token, authenticated}` |
| `IntrospectResponse` | `{valid}` |
| `ProductResponse` | Thông tin sản phẩm |
| `CategoryResponse` | Thông tin danh mục |
| `CartItemResponse` | Item giỏ hàng + subtotal |
| `OrderResponse` | Thông tin đơn hàng + danh sách items |
| `OrderItemResponse` | 1 dòng trong đơn hàng + subtotal |
| `PaymentUrlResponse` | `{payUrl, orderId, amountVnd}` |

### VNPay DTOs
| Class | Nội dung |
|-------|---------|
| `VNPayIpnResponse` | Response trả về VNPay server sau IPN: `{RspCode, Message}` |

---

## 9. Module Mapper

Dùng **MapStruct** — tự động generate implementation tại compile time.

| Mapper | Convert |
|--------|---------|
| `UserMapper` | `UserCreationRequest` → `User`, `User` → `UserResponse` |
| `ProductMapper` | `ProductRequest` → `Product`, `Product` → `ProductResponse` |
| `CategoryMapper` | `CategoryRequest` → `Category`, `Category` → `CategoryResponse` |

> **Lưu ý:** Cần chạy `mvn clean compile` sau khi thay đổi Mapper để re-generate implementation (`*MapperImpl.java` trong `target/generated-sources/`).

---

## 10. Module Exception

### `AppException`
Custom RuntimeException chứa `Errorcode` enum.

```java
throw new AppException(Errorcode.ORDER_NOT_FOUND);
```

### `Errorcode` enum
Mỗi error code chứa: `int code`, `String message`, `HttpStatus`.

| Code | Tên | HTTP Status | Mô tả |
|------|-----|-------------|-------|
| 1001 | USER_EXISTS | 400 | Username đã tồn tại |
| 1007 | USER_NOT_FOUND | 404 | User không tìm thấy |
| 1008 | INVALID_PASSWORD | 400 | Sai mật khẩu |
| 1009 | ACCESS_DENIED | 403 | Không có quyền |
| 1011 | UNAUTHENTICATED | 401 | Chưa đăng nhập |
| 2001 | PRODUCT_NOT_FOUND | 404 | Sản phẩm không tồn tại |
| 2002 | CART_ITEM_NOT_FOUND | 404 | Cart item không tồn tại |
| 2003 | ORDER_NOT_FOUND | 404 | Đơn hàng không tồn tại |
| 2004 | INSUFFICIENT_STOCK | 400 | Không đủ hàng trong kho |
| 2005 | CART_EMPTY | 400 | Giỏ hàng trống |
| 2006 | ORDER_CANNOT_CANCEL | 400 | Đơn không thể hủy |
| 2007 | CATEGORY_NOT_FOUND | 404 | Danh mục không tồn tại |
| 2008 | PAYMENT_CREATE_FAILED | 502 | Tạo thanh toán thất bại |
| 2009 | PAYMENT_NOT_FOUND | 404 | Payment không tồn tại |
| 9999 | UNCATEGORIZED_ERROR | 400 | Lỗi không xác định |

### `GlobalExeptionHandler`
`@ControllerAdvice` bắt và format tất cả exception:
- `AppException` → trả đúng HTTP status của Errorcode
- `RuntimeException` → 400 + code 9999
- `AccessDeniedException` → 403
- `MethodArgumentNotValidException` → 400 + message validation

---

## 11. Module Enums

| Enum | Giá trị |
|------|---------|
| `Role` | `USER`, `ADMIN` |
| `OrderStatus` | `PENDING`, `COMPLETED`, `CANCELLED` |
| `PaymentStatus` | `PENDING`, `SUCCESS`, `FAILED` |
| `PaymentMethod` | `VNPAY`, `DEMO` |

---

## 12. Module Validator

Custom annotation validation cho date of birth:

- `@DobConstraint` — annotation đặt trên field `dob`
- `DobValidator` — implements `ConstraintValidator`, kiểm tra tuổi tối thiểu (cấu hình qua `min`)

---

## 13. Module Messaging (Redis)

> Chỉ active khi `redis.enabled=true` trong `application-dev.properties`

### `RedisMessagePublisher`
Publish message (String hoặc `MessageRequest`) lên Redis channel.

### `RedisMessageSubscriber`
Lắng nghe message từ Redis channel và xử lý (log).

### `RedisConfig`
- Cấu hình `RedisTemplate` với serializer JSON
- Cấu hình `MessageListenerAdapter` và `RedisMessageListenerContainer`
- Dùng `@ConditionalOnProperty(name = "redis.enabled", havingValue = "true")` để bật/tắt

---

## 14. Luồng xử lý chính

### Luồng đăng ký & đăng nhập
```
POST /users          → UserService.createRequest() → hash password → lưu DB
POST /auth/token     → AuthService.authenticate() → kiểm tra password → tạo JWT
                     → JWT chứa: sub=username, scope=ROLE_USER, exp=1h
```

### Luồng mua hàng
```
1. Thêm vào giỏ:   POST /cart          → CartService.addToCart()
2. Checkout:        POST /orders        → OrderService.checkout()
                    → Tạo Order(PENDING) + OrderItems + trừ stock + xóa giỏ
3. Thanh toán:      POST /payments/orders/{id}/vnpay
                    → PaymentService.createVNPayPayment()
                    → VNPayService.createPaymentUrl() → URL redirect
                    → Lưu Payment(PENDING)
                    → Trả về { payUrl }
4. User thanh toán: Redirect đến VNPay sandbox → nhập thẻ test
5. VNPay callback:  GET /payments/vnpay/ipn  (server-to-server)
                    → Verify signature
                    → Cập nhật Payment(SUCCESS) + Order(COMPLETED)
6. User redirect:   GET /payments/vnpay/return → hiển thị kết quả
```

### Luồng xác thực JWT
```
Request với Header: Authorization: Bearer <token>
    → SecurityFilterChain
    → CustomJwtDecoder.decode(token)
        → Kiểm tra token trong invalidated_token (blacklist)
        → NimbusJwtDecoder verify HMAC-SHA512
    → JwtAuthenticationConverter
        → Đọc scope → set ROLE_USER / ROLE_ADMIN
    → Controller nhận request đã authenticated
```

---

## 15. Bảng API endpoints

### Format response chung
```json
{
  "code": 1000,
  "message": "Success",
  "result": { ... }
}
```
- `code 1000` = thành công
- `code != 1000` = lỗi (xem bảng Errorcode)

### Swagger UI
Truy cập tại: **http://localhost:8080/swagger-ui.html**

---

## 16. Cấu hình môi trường

File: `src/main/resources/application-dev.properties`

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/spring_boot_app
spring.datasource.username=postgres
spring.datasource.password=<password>

# JWT
jwt.signerKey=<64-char-hex>
jwt.valid-duration=3600       # access token: 1 giờ
jwt.refresh-duration=36000    # refresh token: 10 giờ

# VNPay Sandbox (đăng ký tại sandbox.vnpayment.vn/devreg)
vnpay.tmn-code=DEMOV210
vnpay.hash-secret=RAOEXHYVSDDIIENYWSLDIIZTANLKSOPE
vnpay.base-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=http://localhost:3000/payment/return
vnpay.ipn-url=http://localhost:8080/payments/vnpay/ipn

# Redis (tắt mặc định)
redis.enabled=false
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### Test card VNPay Sandbox

| Thông tin | Giá trị |
|-----------|---------|
| Ngân hàng | NCB |
| Số thẻ | `9704198526191432198` |
| Tên chủ thẻ | `NGUYEN VAN A` |
| Ngày phát hành | `07/15` |
| OTP | `123456` |

### Chạy project

```bash
# Compile (bắt buộc sau khi thay đổi mapper)
mvn clean compile

# Chạy
mvn spring-boot:run

# Build jar
mvn clean package -DskipTests
java -jar target/javaspringboot-*.jar
```
