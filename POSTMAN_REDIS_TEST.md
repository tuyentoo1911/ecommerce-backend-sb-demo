# Test Redis Messaging với Postman

## 📋 Bước 1: Khởi động services

### 1. Chạy Redis
```powershell
docker run --name redis-server -p 6379:6379 -d redis
```

### 2. Chạy Spring Boot app
```powershell
./mvnw spring-boot:run
```

Đợi đến khi thấy:
```
Tomcat started on port 8080 (http) with context path '/'
Started JavaspringbootApplication in X seconds
```

---

## 🧪 Bước 2: Test với Postman

### **Test 1: Publish Text Message**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/messages/publish-text`

**Headers:**
```
Content-Type: text/plain
```

**Body:** (chọn `raw` → `Text`)
```
Hello from Postman!
```

**Response mong đợi:**
```json
{
    "code": 200,
    "message": "Text published successfully",
    "result": "Hello from Postman!"
}
```

**Log console sẽ hiện:**
```
INFO  RedisMessagePublisher : Publishing message: Hello from Postman!
INFO  RedisMessageSubscriber : Received message: Hello from Postman!
INFO  RedisMessageSubscriber : Processing: Hello from Postman!
```

---

### **Test 2: Publish JSON Object Message**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/messages/publish`

**Headers:**
```
Content-Type: application/json
```

**Body:** (chọn `raw` → `JSON`)
```json
{
    "content": "User registration successful",
    "sender": "UserService",
    "type": "notification"
}
```

**Response mong đợi:**
```json
{
    "code": 200,
    "message": "Message published successfully",
    "result": "Message sent to Redis"
}
```

**Log console sẽ hiện:**
```
INFO  MessagingController : Publishing message: MessageRequest(content=User registration successful, sender=UserService, type=notification)
INFO  RedisMessagePublisher : Publishing object: MessageRequest(content=User registration successful, sender=UserService, type=notification)
INFO  RedisMessageSubscriber : Received message: {"content":"User registration successful","sender":"UserService","type":"notification"}
INFO  RedisMessageSubscriber : Processing: {"content":"User registration successful","sender":"UserService","type":"notification"}
```

---

## 📁 Import Postman Collection

Tạo Collection mới trong Postman với 2 requests:

### Collection: **Redis Messaging Tests**

#### Request 1: Publish Text
- Name: `Publish Text Message`
- Method: `POST`
- URL: `http://localhost:8080/api/messages/publish-text`
- Headers: `Content-Type: text/plain`
- Body: `raw` → `Text` → `Hello Redis!`

#### Request 2: Publish JSON
- Name: `Publish JSON Message`
- Method: `POST`
- URL: `http://localhost:8080/api/messages/publish`
- Headers: `Content-Type: application/json`
- Body: `raw` → `JSON` →
```json
{
    "content": "Test notification",
    "sender": "Admin",
    "type": "alert"
}
```

---

## 🔍 Cách verify kết quả

### 1. Check Response trong Postman
- Status: `200 OK`
- Body có `code: 200` và `message: "...published successfully"`

### 2. Check Log trong console
Mở terminal/console nơi chạy Spring Boot, xem log:
```
INFO  RedisMessagePublisher : Publishing...
INFO  RedisMessageSubscriber : Received message...
INFO  RedisMessageSubscriber : Processing...
```

### 3. Monitor Redis trực tiếp (optional)
```powershell
# Vào Redis CLI
docker exec -it redis-server redis-cli

# Subscribe vào channel (terminal riêng)
SUBSCRIBE notification-channel

# Sẽ thấy messages khi bạn gửi từ Postman
```

---

## 🎯 Các ví dụ message khác

### Notification Welcome
```json
{
    "content": "Welcome to our platform!",
    "sender": "System",
    "type": "welcome"
}
```

### Alert Message
```json
{
    "content": "Suspicious login detected from new IP",
    "sender": "SecurityService",
    "type": "security_alert"
}
```

### Event Message
```json
{
    "content": "User updated profile information",
    "sender": "UserService",
    "type": "user_event"
}
```

### System Maintenance
```json
{
    "content": "System maintenance scheduled at 2AM",
    "sender": "AdminPanel",
    "type": "maintenance"
}
```

---

## ❌ Troubleshooting

### Lỗi: Connection refused
**Nguyên nhân:** Redis chưa chạy

**Giải pháp:**
```powershell
docker ps  # Kiểm tra Redis có chạy không
docker start redis-server  # Nếu stopped
```

### Lỗi: 404 Not Found
**Nguyên nhân:** Endpoint không tồn tại hoặc `redis.enabled=false`

**Giải pháp:**
- Check URL đúng: `http://localhost:8080/api/messages/publish`
- Kiểm tra `application-dev.properties` có `redis.enabled=true`

### Không thấy log Subscriber
**Nguyên nhân:** Channel không khớp hoặc Subscriber chưa được khởi tạo

**Giải pháp:**
- Restart app
- Check log khi app start: nên thấy Redis connection được establish

---

## 📊 Test nâng cao

### Gửi nhiều messages liên tiếp
1. Gửi 5-10 requests nhanh từ Postman
2. Xem console - tất cả messages sẽ được nhận và xử lý
3. Redis pub/sub rất nhanh, xử lý real-time

### Test với nhiều instances
1. Chạy 2 instances của app (port 8080 và 8081)
2. Gửi message từ Postman tới port 8080
3. Cả 2 instances sẽ nhận được message (broadcast)

---

## 💡 Tips

1. **Save requests vào Collection** để test lại dễ dàng
2. **Dùng Variables** trong Postman:
   - `{{baseUrl}}` = `http://localhost:8080`
   - URL thành: `{{baseUrl}}/api/messages/publish`
3. **Dùng Tests tab** để auto-verify response:
```javascript
pm.test("Status is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Message published", function () {
    var json = pm.response.json();
    pm.expect(json.code).to.eql(200);
});
```

Chúc bạn test thành công! 🎉
