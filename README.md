# E-Commerce Microservices Platform 🛒

A comprehensive e-commerce platform built using **Spring Boot Microservices Architecture** with **Spring Cloud** components, featuring role-based access control (RBAC) and JWT authentication.

---

## 👥 Team Members & Responsibilities

| Team Member | Role | Services | Key Responsibilities |
|-------------|------|----------|---------------------|
| **Anand, Prashant** | Backend Developer | **Cart Service** (Port: 8083) | Shopping cart management, add/remove items, quantity updates, cart-to-order conversion |
| **Kakkar, Pratham** | Backend Developer | **Order Service** (Port: 8084) | Order creation, order tracking, status management, payment processing |
| **Kumar, Jayant** | Backend Developer | **User Service** (Port: 8081) | User & Admin authentication, JWT token generation, profile management, role-based access |
| **M, Varshini** | Backend Developer | **Product Service** (Port: 8082) | Product catalog management, CRUD operations, inventory management |

---

## 🏗️ Microservices Architecture

```
                    ┌─────────────────┐       ┌─────────────────┐
                    │  Eureka Server  │       │  Config Server  │
                    │   (Port: 8761)  │       │   (Port: 8888)  │
                    └────────┬────────┘       └────────┬────────┘
                             │                         │
                ┌────────────┴─────────────────────────┴──────────┐
                │                                                 │
                ▼                                                 ▼
        ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
        │ User Service │    │Product Service│   │ Cart Service │
        │  (8081)      │    │  (8082)       │   │  (8083)      │
        └──────────────┘    └──────────────┘    └──────────────┘
                │                   │                    │
                └───────────────────┼────────────────────┘
                                    │
                    ┌───────────────▼───────────────┐
                    │      API Gateway (8080)       │
                    │   JWT Validation & Routing    │
                    └───────────────┬───────────────┘
                                    │
                        ┌───────────┴───────────┐
                        │                       │
                ┌──────────────┐        ┌──────────────┐
                │Order Service │        │    MySQL     │
                │  (8084)      │        │  Databases   │
                └──────────────┘        └──────────────┘
```

---

## 🔐 Authentication Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                        API GATEWAY                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. CHECK TOKEN EXISTS                                              │
│     ├─ No token → 401 Unauthorized                                  │
│     └─ Token exists → Continue                                      │
│                                                                     │
│  2. VALIDATE TOKEN                                                  │
│     ├─ Invalid/Expired → 401 Unauthorized                           │
│     └─ Valid → Extract userId, role, email                          │
│                                                                     │
│  3. ROLE-BASED ACCESS CHECK                                         │
│     ├─ /api/user/**  → Role must be USER                            │
│     ├─ /api/admin/** → Role must be ADMIN                           │
│     └─ /api/both/**  → Role can be USER or ADMIN                    │
│                                                                     │
│  4. PASS HEADERS TO SERVICE                                         │
│     ├─ X-User-Id: {userId}                                          │
│     ├─ X-User-Role: {role}                                          │
│     └─ X-User-Email: {email}                                        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔑 Endpoint Access Control

| Endpoint Pattern | Required Role | Description |
|-----------------|---------------|-------------|
| `/api/auth/**` | **None (Public)** | Registration & Login |
| `/api/user/**` | **USER only** | User operations |
| `/api/admin/**` | **ADMIN only** | Admin operations |
| `/api/both/**` | **USER or ADMIN** | Shared operations |

---

## 📊 Service Ports

| Service | Port |
|---------|------|
| Eureka Server | 8761 |
| Config Server | 8888 |
| API Gateway | 8080 |
| User Service | 8081 |
| Product Service | 8082 |
| Cart Service | 8083 |
| Order Service | 8084 |

---

## 🚀 Quick Start

### Start Services (In Order)
```bash
1. Eureka Server    → cd eureka && mvn spring-boot:run
2. Config Server    → cd config-server && mvn spring-boot:run
3. User Service     → cd userService && mvn spring-boot:run
4. Product Service  → cd product-service && mvn spring-boot:run
5. Cart Service     → cd cart-service && mvn spring-boot:run
6. Order Service    → cd OrderService && mvn spring-boot:run
7. API Gateway      → cd api-gateway && mvn spring-boot:run
```

### Verify Services
- Eureka Dashboard: http://localhost:8761

---

# 📝 COMPLETE API WALKTHROUGH

**Base URL:** `http://localhost:8080`

---

# 🔓 PUBLIC ENDPOINTS (No Token Required)

---

## 1. User Registration

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "shippingAddress": "123 Main Street, City, State 12345",
    "paymentDetails": "Credit Card"
}
```

**Response:** `201 CREATED`
```json
{
    "userId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "USER"
}
```

---

## 2. User Login

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "john@example.com",
    "password": "password123"
}
```

**Response:** `200 OK`
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "USER"
}
```

⚠️ **SAVE THIS TOKEN! Valid for 30 minutes.**

---

## 3. Admin Registration (Requires Secret Key)

```http
POST http://localhost:8080/api/auth/admin/register
Content-Type: application/json

{
    "name": "Admin User",
    "email": "admin@example.com",
    "password": "admin123",
    "adminSecretKey": "ADMIN_SECRET_KEY_2024"
}
```

**Response:** `201 CREATED`

---

## 4. Admin Login

```http
POST http://localhost:8080/api/auth/admin/login
Content-Type: application/json

{
    "email": "admin@example.com",
    "password": "admin123"
}
```

**Response:** `200 OK`
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 2,
    "name": "Admin User",
    "email": "admin@example.com",
    "role": "ADMIN"
}
```

⚠️ **SAVE THIS TOKEN! Valid for 30 minutes.**

---

# 🔴 ADMIN ONLY ENDPOINTS (/api/admin/**)

**Header Required:** `Authorization: Bearer <admin_token>`

---

## 5. Create Product

```http
POST http://localhost:8080/api/admin/products
Authorization: Bearer <admin_token>
Content-Type: application/json

{
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse",
    "price": 29.99,
    "category": "Electronics",
    "stock": 150,
    "imageUrl": "https://example.com/mouse.jpg"
}
```

**Response:** `200 OK`

---

## 6. Update Product

```http
PUT http://localhost:8080/api/admin/products/1
Authorization: Bearer <admin_token>
Content-Type: application/json

{
    "name": "Wireless Mouse Pro",
    "description": "Ergonomic wireless mouse - Updated",
    "price": 34.99,
    "category": "Electronics",
    "stock": 200,
    "imageUrl": "https://example.com/mouse-pro.jpg"
}
```

**Response:** `200 OK`

---

## 7. Delete Product

```http
DELETE http://localhost:8080/api/admin/products/1
Authorization: Bearer <admin_token>
```

**Response:** `204 NO CONTENT`

---

## 8. Get All Users

```http
GET http://localhost:8080/api/admin/users
Authorization: Bearer <admin_token>
```

**Response:** `200 OK`
```json
[
    {
        "userId": 1,
        "name": "John Doe",
        "email": "john@example.com",
        "role": "USER"
    },
    {
        "userId": 2,
        "name": "Admin User",
        "email": "admin@example.com",
        "role": "ADMIN"
    }
]
```

---

## 9. Get User by ID

```http
GET http://localhost:8080/api/admin/users/1
Authorization: Bearer <admin_token>
```

**Response:** `200 OK`

---

## 10. Delete User

```http
DELETE http://localhost:8080/api/admin/users/3
Authorization: Bearer <admin_token>
```

**Response:** `200 OK`

---

## 11. Get Orders by User ID

```http
GET http://localhost:8080/api/admin/orders/user/1
Authorization: Bearer <admin_token>
```

**Response:** `200 OK`

---

## 12. Update Order Status

```http
PATCH http://localhost:8080/api/admin/orders/1/status?status=SHIPPED
Authorization: Bearer <admin_token>
```

**Available Statuses:** `CREATED`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`

---

## 13. Update Payment Status

```http
PATCH http://localhost:8080/api/admin/orders/1/payment?paymentStatus=PAID
Authorization: Bearer <admin_token>
```

**Available Payment Statuses:** `PENDING`, `PAID`, `FAILED`, `REFUNDED`

---

# 🔵 USER ONLY ENDPOINTS (/api/user/**)

**Header Required:** `Authorization: Bearer <user_token>`

---

## 14. Add to Cart

```http
POST http://localhost:8080/api/user/cart/add/1?quantity=2
Authorization: Bearer <user_token>
```

**Response:** `200 OK`

---

## 15. View My Cart

```http
GET http://localhost:8080/api/user/cart
Authorization: Bearer <user_token>
```

**Response:** `200 OK`

---

## 16. Increment Cart Item

```http
PUT http://localhost:8080/api/user/cart/1/increment
Authorization: Bearer <user_token>
```

**Response:** `200 OK`

---

## 17. Decrement Cart Item

```http
PUT http://localhost:8080/api/user/cart/1/decrement
Authorization: Bearer <user_token>
```

**Response:** `200 OK`

---

## 18. Remove from Cart

```http
DELETE http://localhost:8080/api/user/cart/1
Authorization: Bearer <user_token>
```

**Response:** `200 OK`

---

## 19. Place Order

```http
POST http://localhost:8080/api/user/cart/place-order
Authorization: Bearer <user_token>
```

**Response:** `201 CREATED`

---

## 20. View My Orders

```http
GET http://localhost:8080/api/user/orders
Authorization: Bearer <user_token>
```

**Response:** `200 OK`

---

## 21. Update Name

```http
PATCH http://localhost:8080/api/user/profile/name
Authorization: Bearer <user_token>
Content-Type: application/json

{
    "name": "John Smith"
}
```

**Response:** `200 OK`

---

## 22. Update Email

```http
PATCH http://localhost:8080/api/user/profile/email
Authorization: Bearer <user_token>
Content-Type: application/json

{
    "email": "johnsmith@example.com"
}
```

**Response:** `200 OK`

---

## 23. Update Password

```http
PATCH http://localhost:8080/api/user/profile/password
Authorization: Bearer <user_token>
Content-Type: application/json

{
    "currentPassword": "password123",
    "newPassword": "newpassword456"
}
```

**Response:** `200 OK`

---

## 24. Update Address

```http
PATCH http://localhost:8080/api/user/profile/address
Authorization: Bearer <user_token>
Content-Type: application/json

{
    "shippingAddress": "456 Oak Avenue, Town, State 67890"
}
```

**Response:** `200 OK`

---

## 25. Update Payment Details

```http
PATCH http://localhost:8080/api/user/profile/payment
Authorization: Bearer <user_token>
Content-Type: application/json

{
    "paymentDetails": "Debit Card ****5678"
}
```

**Response:** `200 OK`

---

## 26. Delete Account

```http
DELETE http://localhost:8080/api/user/profile
Authorization: Bearer <user_token>
```

**Response:** `200 OK`

---

# 🟢 BOTH USER & ADMIN ENDPOINTS (/api/both/**)

**Header Required:** `Authorization: Bearer <user_token>` or `Authorization: Bearer <admin_token>`

---

## 27. View All Products

```http
GET http://localhost:8080/api/both/products
Authorization: Bearer <token>
```

**Response:** `200 OK`

---

## 28. View Product by ID

```http
GET http://localhost:8080/api/both/products/1
Authorization: Bearer <token>
```

**Response:** `200 OK`

---

## 29. Track Order

```http
GET http://localhost:8080/api/both/orders/1
Authorization: Bearer <token>
```

**Response:** `200 OK`

---

# 📋 QUICK REFERENCE

## 🔓 PUBLIC ENDPOINTS

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | User registration |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/admin/register` | Admin registration |
| POST | `/api/auth/admin/login` | Admin login |

## 🔴 ADMIN ONLY ENDPOINTS

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/products` | Create product |
| PUT | `/api/admin/products/{id}` | Update product |
| DELETE | `/api/admin/products/{id}` | Delete product |
| GET | `/api/admin/users` | Get all users |
| GET | `/api/admin/users/{id}` | Get user by ID |
| DELETE | `/api/admin/users/{id}` | Delete user |
| GET | `/api/admin/orders/user/{userId}` | Get orders by user |
| PATCH | `/api/admin/orders/{id}/status` | Update order status |
| PATCH | `/api/admin/orders/{id}/payment` | Update payment status |

## 🔵 USER ONLY ENDPOINTS

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/user/cart/add/{productId}` | Add to cart |
| GET | `/api/user/cart` | View cart |
| PUT | `/api/user/cart/{productId}/increment` | Increment |
| PUT | `/api/user/cart/{productId}/decrement` | Decrement |
| DELETE | `/api/user/cart/{productId}` | Remove from cart |
| POST | `/api/user/cart/place-order` | Place order |
| GET | `/api/user/orders` | View my orders |
| PATCH | `/api/user/profile/name` | Update name |
| PATCH | `/api/user/profile/email` | Update email |
| PATCH | `/api/user/profile/password` | Update password |
| PATCH | `/api/user/profile/address` | Update address |
| PATCH | `/api/user/profile/payment` | Update payment |
| DELETE | `/api/user/profile` | Delete account |

## 🟢 BOTH USER & ADMIN ENDPOINTS

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/both/products` | View all products |
| GET | `/api/both/products/{id}` | View product by ID |
| GET | `/api/both/orders/{id}` | Track order |

---

## 🔑 Important Notes

1. **Token Expiry**: Tokens are valid for **30 minutes** only
2. **Admin Secret Key**: `ADMIN_SECRET_KEY_2024`
3. **Start Services in Order**: Eureka → Config → Services → Gateway
4. **All requests go through**: `http://localhost:8080`
5. **Role Validation**: Done at API Gateway level

---

## 🛠️ Technologies Used

- **Spring Boot 3.x**
- **Spring Cloud Gateway**
- **Spring Cloud Netflix Eureka**
- **Spring Cloud Config**
- **Spring Data JPA**
- **MySQL**
- **JWT (JSON Web Tokens)**
- **OpenFeign**
- **Resilience4j**

---

**© 2024 E-Commerce Microservices Platform - Team Project**

