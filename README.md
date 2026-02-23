# E-Commerce API Testing Guide

## Base URL
```
http://localhost:8080
```

## 1. Authentication

### Login as Admin
```bash
POST /auth/token
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "token": "eyJhbGciOiJSUzI1NiJ9...",
    "authenticated": true
  }
}
```

**Copy token để dùng cho các request tiếp theo:**
```
Authorization: Bearer <token>
```

### Create Normal User
```bash
POST /users
Content-Type: application/json

{
  "username": "customer1",
  "password": "password123",
  "email": "customer1@test.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Login as Customer
```bash
POST /auth/token
Content-Type: application/json

{
  "username": "customer1",
  "password": "password123"
}
```

---

## 2. Product Management (ADMIN)

### Create Products
```bash
POST /products
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 chip",
  "price": 999.99,
  "stock": 50,
  "category": "Electronics"
}
```

```bash
POST /products
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "MacBook Pro M3",
  "description": "Professional laptop",
  "price": 2499.99,
  "stock": 30,
  "category": "Electronics"
}
```

```bash
POST /products
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "AirPods Pro",
  "description": "Wireless earbuds with ANC",
  "price": 249.99,
  "stock": 100,
  "category": "Audio"
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "iPhone 15 Pro",
    "description": "Latest iPhone with A17 chip",
    "price": 999.99,
    "stock": 50,
    "category": "Electronics"
  }
}
```

### Get All Products (Public)
```bash
GET /products
```

### Get Product by ID (Public)
```bash
GET /products/{id}
```

### Update Product (ADMIN)
```bash
PUT /products/{id}
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "name": "iPhone 15 Pro Max",
  "description": "Latest iPhone with A17 chip - Updated",
  "price": 1199.99,
  "stock": 45,
  "category": "Electronics"
}
```

### Delete Product (ADMIN)
```bash
DELETE /products/{id}
Authorization: Bearer <admin_token>
```

---

## 3. Shopping Cart (Customer)

### Add to Cart
```bash
POST /cart/items
Authorization: Bearer <customer_token>
Content-Type: application/json

{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "quantity": 2
}
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "cart-item-id",
    "product": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "iPhone 15 Pro",
      "price": 999.99,
      "stock": 50
    },
    "quantity": 2,
    "subtotal": 1999.98
  }
}
```

### Add More Products to Cart
```bash
POST /cart/items
Authorization: Bearer <customer_token>
Content-Type: application/json

{
  "productId": "airpods-id",
  "quantity": 1
}
```

### View Cart
```bash
GET /cart
Authorization: Bearer <customer_token>
```

**Response:**
```json
{
  "code": 1000,
  "result": [
    {
      "id": "cart-item-1",
      "product": {
        "id": "product-id-1",
        "name": "iPhone 15 Pro",
        "price": 999.99
      },
      "quantity": 2,
      "subtotal": 1999.98
    },
    {
      "id": "cart-item-2",
      "product": {
        "id": "product-id-2",
        "name": "AirPods Pro",
        "price": 249.99
      },
      "quantity": 1,
      "subtotal": 249.99
    }
  ]
}
```

### Update Cart Item Quantity
```bash
PUT /cart/items/{cart_item_id}
Authorization: Bearer <customer_token>
Content-Type: application/json

{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "quantity": 3
}
```

### Remove from Cart
```bash
DELETE /cart/items/{cart_item_id}
Authorization: Bearer <customer_token>
```

---

## 4. Orders

### Checkout (Create Order from Cart)
```bash
POST /orders
Authorization: Bearer <customer_token>
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "order-id",
    "userId": "user-id",
    "username": "customer1",
    "totalPrice": 2249.97,
    "status": "PENDING",
    "createdAt": "2026-02-23T09:30:00",
    "items": [
      {
        "productId": "product-id-1",
        "productName": "iPhone 15 Pro",
        "quantity": 2,
        "priceAtPurchase": 999.99,
        "subtotal": 1999.98
      },
      {
        "productId": "product-id-2",
        "productName": "AirPods Pro",
        "quantity": 1,
        "priceAtPurchase": 249.99,
        "subtotal": 249.99
      }
    ]
  }
}
```

### Get My Orders
```bash
GET /orders/my
Authorization: Bearer <customer_token>
```

### Get All Orders (ADMIN)
```bash
GET /orders
Authorization: Bearer <admin_token>
```

### Cancel Order (Customer)
```bash
PUT /orders/{order_id}/cancel
Authorization: Bearer <customer_token>
```

**Response:**
```json
{
  "code": 1000,
  "result": {
    "id": "order-id",
    "status": "CANCELLED",
    "totalPrice": 2249.97
  }
}
```

### Update Order Status (ADMIN)
```bash
PUT /orders/{order_id}/status?status=COMPLETED
Authorization: Bearer <admin_token>
```

**Available statuses:**
- `PENDING`
- `COMPLETED`
- `CANCELLED`

---

## 5. Test Flow Example

### Complete Purchase Flow:

1. **Admin creates products**
   ```bash
   POST /products (admin token)
   ```

2. **Customer views products**
   ```bash
   GET /products (no token needed)
   ```

3. **Customer adds to cart**
   ```bash
   POST /cart/items (customer token)
   POST /cart/items (customer token)
   ```

4. **Customer views cart**
   ```bash
   GET /cart (customer token)
   ```

5. **Customer checks out**
   ```bash
   POST /orders (customer token)
   # Cart will be cleared automatically
   ```

6. **Customer views order history**
   ```bash
   GET /orders/my (customer token)
   ```

7. **Admin views all orders**
   ```bash
   GET /orders (admin token)
   ```

8. **Admin updates order status**
   ```bash
   PUT /orders/{id}/status?status=COMPLETED (admin token)
   ```

---

## Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 1000 | Success | Request successful |
| 1009 | Access Denied | Insufficient permissions |
| 2001 | Product not found | Product doesn't exist |
| 2002 | Cart item not found | Cart item doesn't exist |
| 2003 | Order not found | Order doesn't exist |
| 2004 | Insufficient stock | Not enough product in stock |
| 2005 | Cart is empty | Cannot checkout empty cart |
| 2006 | Order cannot be cancelled | Order already completed/cancelled |

---

## Swagger UI

Test APIs interactively:
```
http://localhost:8080/swagger-ui.html
```

1. Click **Authorize** button
2. Enter: `Bearer <your_token>`
3. Test any endpoint

---

## Notes

- Products GET endpoints are **public** (no token needed)
- Cart & Orders require **customer token**
- Product CRUD (create/update/delete) requires **ADMIN token**
- Stock is automatically reduced on checkout
- Stock is restored if order is cancelled
- Cart is cleared after successful checkout
