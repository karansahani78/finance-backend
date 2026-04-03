# 💰 Finance Data Processing & Access Control Backend

A **production-grade REST API** for a finance dashboard system, built with **Spring Boot 3**, featuring secure authentication, role-based access control, and scalable architecture.

---

## 🚀 Tech Stack

* **Java 17** + **Spring Boot 3.2**
* **PostgreSQL 15** (Primary Database)
* **Spring Security + JWT** (Stateless Authentication)
* **Spring Data JPA + Hibernate**
* **Bucket4j** (Rate Limiting)
* **Testcontainers + JUnit 5** (Integration Testing)
* **Lombok** (Boilerplate Reduction)

---

## 🧱 Project Architecture

```
src/
├── main/java/com/finance/
│   ├── config/            # Security & app configuration
│   ├── controller/        # REST controllers (Auth, Users, Transactions, Dashboard)
│   ├── dto/
│   │   ├── request/       # Input DTOs with validation
│   │   └── response/      # Output DTOs (no entity exposure)
│   ├── exception/         # Global exception handling (RFC 9457 ProblemDetail)
│   ├── filter/            # Rate limiting (Bucket4j)
│   ├── model/             # JPA entities
│   │   └── enums/         # Domain enums
│   ├── repository/        # Data access layer (Spring Data JPA)
│   ├── security/          # JWT, filters, UserDetailsService
│   └── service/           # Business logic layer
└── main/resources/
    └── application.yml
```

---

## ⚙️ Getting Started

### Prerequisites

* Java 17+
* Maven 3.9+
* PostgreSQL 15+ (local or Docker)

### 1. Create Database

```sql
CREATE DATABASE financedb;
```

### 2. Configure Application

Update `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/financedb
    username: your_pg_user
    password: your_pg_password
```

### 3. Run Application

```bash
mvn spring-boot:run
```

---

## 👤 Default Users

| Role    | Email                                           | Password    |
| ------- | ----------------------------------------------- | ----------- |
| ADMIN   | [admin@finance.io](mailto:admin@finance.io)     | password123 |
| ANALYST | [analyst@finance.io](mailto:analyst@finance.io) | password123 |
| VIEWER  | [viewer@finance.io](mailto:viewer@finance.io)   | password123 |

---

## 🔐 Role Permissions

| Endpoint                      | VIEWER | ANALYST | ADMIN |
| ----------------------------- | ------ | ------- | ----- |
| POST /api/auth/login          | ✓      | ✓       | ✓     |
| GET /api/transactions/**      | ✓      | ✓       | ✓     |
| POST/PUT/DELETE /transactions | ✗      | ✗       | ✓     |
| GET /api/dashboard/**         | ✗      | ✓       | ✓     |
| /api/users/**                 | ✗      | ✗       | ✓     |

---

## 🏗️ Architecture Diagram

![Architecture Diagram](https://github.com/user-attachments/assets/52be7082-739b-4bfa-8ee4-4b9279018131)

🔗 High-level design: [https://app.eraser.io/workspace/bUsiGLd88P39hGYSbHHu](https://app.eraser.io/workspace/bUsiGLd88P39hGYSbHHu)

---

## 📡 API Reference

### 🔑 Auth

#### POST `/api/auth/login`

```json
{
  "email": "admin@finance.io",
  "password": "password123"
}
```

---

### 💸 Transactions

#### POST `/api/transactions`

```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "SALARY",
  "txnDate": "2024-03-01",
  "description": "March salary"
}
```

#### GET `/api/transactions`

Supports filters, pagination, and sorting.

---

### 👥 Users (Admin Only)

#### POST `/api/users`

```json
{
  "name": "Jane Doe",
  "email": "jane@finance.io",
  "password": "strongpassword",
  "role": "ANALYST"
}
```

---

### 📊 Dashboard

#### GET `/api/dashboard/summary`

Returns aggregated financial insights:

* Total Income / Expenses
* Net Balance
* Category breakdowns
* Trends (monthly & weekly)

---

## ⚠️ Error Handling

Standardized using **RFC 9457 ProblemDetail**:

```json
{
  "status": 400,
  "detail": "Validation failed",
  "timestamp": "2024-03-15T10:30:00Z"
}
```

---

## 🚦 Rate Limiting

* **60 requests/minute per IP**

```yaml
rate-limit:
  capacity: 60
  refill-per-minute: 60
```

---

## 🌐 Live API

Swagger UI:

👉 [https://your-app.onrender.com/swagger-ui/index.html](https://your-app.onrender.com/swagger-ui/index.html)

---

## 🧪 Testing

```bash
mvn test
```

Includes:

* Unit Tests (Mockito)
* Integration Tests (Testcontainers + PostgreSQL)

---

## 🧠 Design Decisions

* Soft deletes via `deleted_at`
* DTO-based API responses (no entity exposure)
* Centralized security config
* Stateless JWT authentication
* In-memory rate limiting (scalable via Redis)
* Production-ready error handling

---

## 📌 Notes

* JWT secret should be externalized in production
* Database migrations handled via Flyway (recommended)
* Suitable for cloud deployment (Render, AWS, etc.)

---

## ⭐ Summary

This project demonstrates **clean architecture, secure API design, and production-ready backend practices**, making it suitable for real-world finance systems and backend engineering roles.
