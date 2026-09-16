# API Reference Manual
## ShopSphere / Go N Buy Platform

All APIs are exposed behind the NGINX Reverse Proxy on port `80` (or directly on their microservice ports in dev mode).

---

### 1. Authentication Service (`:8081` / `/api/auth`)

#### Register User
`POST /api/auth/register`
```json
// Request
{
  "username": "alexsmith",
  "email": "alex@example.com",
  "password": "SecurePassword123!",
  "firstName": "Alex",
  "lastName": "Smith"
}

// Response: 201 Created
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "alexsmith",
  "role": "USER"
}
```

#### User Login
`POST /api/auth/login`
```json
// Request
{
  "username": "alexsmith",
  "password": "SecurePassword123!"
}

// Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "alexsmith",
  "role": "USER"
}
```

---

### 2. Product Service (`:8082` / `/api/products`, `/api/categories`)

#### Get Product Catalog
`GET /api/products?category={catId}&search={term}&page=0&size=20`
```json
// Response: 200 OK
{
  "content": [
    {
      "id": "c1f7b889-f55a-4e67-9c98-1e4277b0c301",
      "name": "Cloud Native Kubernetes Handbook",
      "description": "Comprehensive guide to multi-cloud production Kubernetes.",
      "price": 49.99,
      "sku": "BK-K8S-001",
      "availableStock": 25,
      "category": { "id": "cat-books", "name": "Books" }
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 3. Cart Service (`:8083` / `/api/cart`)
*Requires `Authorization: Bearer <token>`*

#### Add Item to Cart
`POST /api/cart/items`
```json
// Request
{
  "productId": "c1f7b889-f55a-4e67-9c98-1e4277b0c301",
  "productName": "Cloud Native Kubernetes Handbook",
  "unitPrice": 49.99,
  "quantity": 2
}

// Response: 200 OK
{
  "userId": "alexsmith",
  "items": [
    {
      "productId": "c1f7b889-f55a-4e67-9c98-1e4277b0c301",
      "productName": "Cloud Native Kubernetes Handbook",
      "unitPrice": 49.99,
      "quantity": 2,
      "subtotal": 99.98
    }
  ],
  "totalAmount": 99.98,
  "itemCount": 2
}
```

---

### 4. Order Service (`:8084` / `/api/orders`)
*Requires `Authorization: Bearer <token>` and optional `Idempotency-Key`*

#### Create Order
`POST /api/orders`
```http
Idempotency-Key: b7ea4182-1fa9-4a4b-84a1-098e94fae751
```
```json
// Request
{
  "shippingAddress": "123 Cloud Way, San Francisco CA 94105, US",
  "items": [
    {
      "productId": "c1f7b889-f55a-4e67-9c98-1e4277b0c301",
      "quantity": 2
    }
  ]
}

// Response: 201 Created
{
  "id": "e4b9d031-6e3e-4b68-b7aa-d0b7d722d302",
  "userId": "alexsmith",
  "status": "CONFIRMED",
  "totalAmount": 99.98,
  "shippingAddress": "123 Cloud Way, San Francisco CA 94105, US",
  "createdAt": "2026-09-15T18:00:00.000Z",
  "items": [ ... ]
}
```

#### Cancel Order
`POST /api/orders/{id}/cancel`
```json
// Response: 200 OK
{
  "id": "e4b9d031-6e3e-4b68-b7aa-d0b7d722d302",
  "status": "CANCELLED"
}
```

---

### 5. Inventory Service (`:8085` / `/api/inventory`)

#### Check Stock
`GET /api/inventory/{productId}`
```json
// Response: 200 OK
{
  "productId": "c1f7b889-f55a-4e67-9c98-1e4277b0c301",
  "sku": "BK-K8S-001",
  "availableStock": 23,
  "reservedStock": 2
}
```

---

### 6. Notification Service (`:8086` / `/api/notifications`)
*Requires `Authorization: Bearer <token>`*

#### Get Notifications
`GET /api/notifications`
```json
// Response: 200 OK
[
  {
    "id": "713bc498-ea9a-4c28-9ba0-f38b25f46401",
    "userId": "alexsmith",
    "eventType": "ORDER_CONFIRMED",
    "channel": "EMAIL",
    "recipient": "alex@example.com",
    "subject": "Order Confirmed! - #e4b9d031-6e3e-4b68-b7aa-d0b7d722d302",
    "body": "Great news! Your order has been confirmed and payment was successful.",
    "status": "SIMULATED",
    "createdAt": "2026-09-15T18:00:02.000Z"
  }
]
```
