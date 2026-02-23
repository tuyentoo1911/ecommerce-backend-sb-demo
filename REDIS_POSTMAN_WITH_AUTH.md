# Test Redis Messaging với Authentication

## ✅ Cách 1: Test không cần JWT Token (Đã cấu hình)

**SecurityConfig** đã được cập nhật để cho phép test messaging endpoints mà không cần token.

Restart app và test ngay:
```powershell
./mvnw spring-boot:run
```

Test trong Postman:
- URL: `http://localhost:8080/api/messages/publish-text`
- Không cần thêm Authorization header
- Gửi trực tiếp!

---

## 🔐 Cách 2: Test với JWT Token (Production-ready)

Nếu muốn bảo mật endpoints trong production, làm theo các bước sau:

### Bước 1: Đăng ký user (nếu chưa có)

**Method:** `POST`  
**URL:** `http://localhost:8080/users`  
**Body:**
```json
{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "dob": "1990-01-01"
}
```

### Bước 2: Login để lấy JWT Token

**Method:** `POST`  
**URL:** `http://localhost:8080/auth/token`  
**Body:**
```json
{
    "username": "admin",
    "password": "admin"
}
```

**Response:**
```json
{
    "code": 200,
    "result": {
        "token": "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJkZXZhdHQuY29tIiwic3ViIjoiYWRtaW4iLCJleHAiOjE3MDc0ODcyMTUsImlhdCI6MTcwNzQ4MzYxNSwic2NvcGUiOiJBRE1JTiJ9...",
        "expiryTime": 1707487215000,
        "authenticated": true
    }
}
```

**Copy token** từ response.

### Bước 3: Dùng Token trong Postman

**Cách 1: Authorization Tab**
1. Chọn tab **Authorization**
2. Type: **Bearer Token**
3. Paste token vào ô **Token**

**Cách 2: Headers Tab**
1. Chọn tab **Headers**
2. Add header:
   - Key: `Authorization`
   - Value: `Bearer eyJhbGciOiJIUzUxMiJ9...` (thêm "Bearer " trước token)

### Bước 4: Test Redis Messaging với Token

**Method:** `POST`  
**URL:** `http://localhost:8080/api/messages/publish`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```
**Body:**
```json
{
    "content": "Authenticated message",
    "sender": "testuser",
    "type": "notification"
}
```

**Response thành công:**
```json
{
    "code": 200,
    "message": "Message published successfully",
    "result": "Message sent to Redis"
}
```

---

## 🔄 Sử dụng Variables trong Postman (Recommended)

### Setup Collection Variables:

1. Click vào Collection → **Variables** tab
2. Thêm variables:

| Variable | Initial Value | Current Value |
|----------|--------------|---------------|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |
| `token` | (để trống) | (paste token sau khi login) |

### Update URLs:
```
{{baseUrl}}/api/messages/publish
```

### Set Token tự động:

Thêm **Test script** vào Login request:
```javascript
// Tab "Tests" của request /auth/token
var jsonData = pm.response.json();
if (jsonData.result && jsonData.result.token) {
    pm.collectionVariables.set("token", jsonData.result.token);
    console.log("Token saved:", jsonData.result.token);
}
```

### Dùng Token từ Variable:

Authorization Tab:
- Type: **Bearer Token**
- Token: `{{token}}`

Giờ chỉ cần:
1. Login 1 lần → Token tự động lưu
2. Các request sau dùng `{{token}}` → Tự động có token

---

## 🛡️ Production Setup

Nếu deploy production, **xóa** `/api/messages/**` khỏi PUBLIC_ENDPOINTS:

```java
private final String[] PUBLIC_ENDPOINTS = { 
    "/users", 
    "/auth/token", 
    "/auth/introspect", 
    "/auth/logout", 
    "/auth/refresh"
    // Không có /api/messages/** - yêu cầu authentication
};
```

Restart app và messaging endpoints sẽ yêu cầu JWT token.

---

## ❌ Common Errors

### Error: "Authentication required"
**Nguyên nhân:** Endpoint cần token nhưng không có hoặc sai

**Giải pháp:**
- Kiểm tra Authorization header có đúng format: `Bearer <token>`
- Token còn hạn chưa? (mặc định 3600s = 1 giờ)
- Login lại để lấy token mới

### Error: "Invalid token"
**Nguyên nhân:** Token sai hoặc đã hết hạn

**Giải pháp:**
- Login lại: `POST /auth/token`
- Copy token mới
- Update lại Authorization header

### Error: "Token expired"
**Nguyên nhân:** Token đã hết hạn (sau 1 giờ)

**Giải pháp:**
- Login lại hoặc dùng Refresh Token:
```json
POST /auth/refresh
{
    "token": "your_refresh_token"
}
```

---

## 📋 Postman Collection với Auth

Import collection có sẵn authentication flow:

**File:** `Redis_Messaging_Tests.postman_collection.json`

Requests trong collection:
1. ✅ **Login** - Lấy token
2. ✅ **Publish Text** - Với auth
3. ✅ **Publish JSON** - Với auth

Workflow:
1. Run request "Login" trước
2. Token tự động lưu vào variable
3. Các request sau tự động dùng token

---

## 💡 Tips

### Auto-refresh Token
Thêm Pre-request Script vào Collection:
```javascript
// Check token expiry
var expiryTime = pm.collectionVariables.get("tokenExpiry");
var now = Date.now();

if (!expiryTime || now > expiryTime - 60000) { // Refresh 1 phút trước khi hết hạn
    pm.sendRequest({
        url: pm.collectionVariables.get("baseUrl") + "/auth/token",
        method: 'POST',
        header: {
            'Content-Type': 'application/json'
        },
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                username: "admin",
                password: "admin"
            })
        }
    }, function (err, response) {
        if (!err) {
            var json = response.json();
            pm.collectionVariables.set("token", json.result.token);
            pm.collectionVariables.set("tokenExpiry", json.result.expiryTime);
        }
    });
}
```

Giờ token sẽ tự động refresh khi sắp hết hạn! 🎉
