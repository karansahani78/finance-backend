# Finance Data Processing & Access Control Backend

A production-oriented REST API backend for a finance dashboard system. Built with Spring Boot 3, PostgreSQL, JWT auth, and role-based access control.

---

## Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **PostgreSQL 15** (primary datastore)
- **Spring Security** with stateless JWT authentication
- **Spring Data JPA** + **Hibernate** (ORM)
- **Flyway** (schema migrations)
- **Bucket4j** (in-memory per-IP rate limiting)
- **Testcontainers** + **JUnit 5** (integration tests with real Postgres)
- **Lombok** (boilerplate reduction)

---

## Project Layout

```
src/
├── main/java/com/finance/
│   ├── config/            # Security config
│   ├── controller/        # REST layer (Auth, User, Transaction, Dashboard)
│   ├── dto/
│   │   ├── request/       # Input DTOs with validation annotations
│   │   └── response/      # Output DTOs (never expose entities directly)
│   ├── exception/         # Custom exceptions + global handler (RFC 9457 ProblemDetail)
│   ├── filter/            # Rate limit filter (Bucket4j)
│   ├── model/             # JPA entities
│   │   └── enums/         # Role, UserStatus, TransactionType, TransactionCategory
│   ├── repository/        # Spring Data JPA repos with custom JPQL queries
│   ├── security/          # JWT util, auth filter, UserDetailsService
│   └── service/           # Business logic layer
└── main/resources/
    ├── application.yml
    ├── application-test.yml
    └── db/migration/      # Flyway versioned SQL scripts
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 15+ running locally (or Docker)

### 1. Create the database

```sql
CREATE DATABASE financedb;
```

### 2. Configure credentials

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/financedb
    username: your_pg_user
    password: your_pg_password
```

### 3. Run the application

```bash
mvn spring-boot:run
```

Flyway will automatically run the migrations and seed three default users on first boot.

---

## Default Users (seeded via Flyway)

| Role    | Email                 | Password    |
|---------|-----------------------|-------------|
| ADMIN   | admin@finance.io      | password123 |
| ANALYST | analyst@finance.io    | password123 |
| VIEWER  | viewer@finance.io     | password123 |

> The bcrypt hash in the seed file corresponds to `password123` with cost factor 12.

---

## Role Permissions

| Endpoint group              | VIEWER | ANALYST | ADMIN |
|-----------------------------|--------|---------|-------|
| POST /api/auth/login        | ✓      | ✓       | ✓     |
| GET  /api/transactions/**   | ✓      | ✓       | ✓     |
| POST/PUT/DELETE /api/transactions/** | ✗ | ✗    | ✓     |
| GET  /api/dashboard/**      | ✗      | ✓       | ✓     |
| /api/users/**               | ✗      | ✗       | ✓     |

---

## API Reference

### Auth

#### POST `/api/auth/login`
```json
{
  "email": "admin@finance.io",
  "password": "password123"
}
```
Response includes a `Bearer` token. Pass it as `Authorization: Bearer <token>` on every subsequent request.

---

### Transactions

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

Valid `type` values: `INCOME`, `EXPENSE`

Valid `category` values: `SALARY`, `FREELANCE`, `INVESTMENT`, `FOOD`, `TRANSPORT`, `ENTERTAINMENT`, `HEALTHCARE`, `UTILITIES`, `EDUCATION`, `SHOPPING`, `RENT`, `INSURANCE`, `OTHER`

#### GET `/api/transactions`

Supports the following query parameters:

| Param      | Type     | Description                              |
|------------|----------|------------------------------------------|
| `type`     | string   | Filter by `INCOME` or `EXPENSE`          |
| `category` | string   | Filter by category name                  |
| `from`     | date     | Start date (ISO format: `YYYY-MM-DD`)    |
| `to`       | date     | End date (ISO format: `YYYY-MM-DD`)      |
| `search`   | string   | Searches within the description field    |
| `page`     | int      | Page number (0-indexed, default: 0)      |
| `size`     | int      | Page size (default: 20, max: 100)        |
| `sort`     | string   | Field to sort by (default: `txnDate`)    |
| `direction`| string   | `asc` or `desc` (default: `desc`)        |

#### GET `/api/transactions/{id}`
#### PUT `/api/transactions/{id}` — partial update, all fields optional
#### DELETE `/api/transactions/{id}` — soft delete (record stays in DB with `deleted_at` set)

---

### Users (Admin only)

#### POST `/api/users`
```json
{
  "name": "Jane Doe",
  "email": "jane@finance.io",
  "password": "strongpassword",
  "role": "ANALYST"
}
```

#### GET `/api/users`

| Param      | Description                       |
|------------|-----------------------------------|
| `search`   | Searches name and email           |
| `page`     | Page number (0-indexed)           |
| `size`     | Page size (max 100)               |
| `sort`     | Field to sort by                  |
| `direction`| `asc` or `desc`                   |

#### GET `/api/users/{id}`
#### PUT `/api/users/{id}` — update name, role, or status (all optional)
#### DELETE `/api/users/{id}` — soft delete, also sets status to `INACTIVE`

---

### Dashboard (Analyst + Admin)

#### GET `/api/dashboard/summary`

Returns:
- `totalIncome` — sum of all income transactions
- `totalExpenses` — sum of all expense transactions
- `netBalance` — income minus expenses
- `incomeByCategory` — map of category → total income
- `expenseByCategory` — map of category → total expenses
- `recentActivity` — last 10 transactions
- `monthlyTrend` — monthly breakdown of income vs expenses
- `weeklyTrend` — weekly breakdown of income vs expenses

---

## Error Handling

All errors follow the RFC 9457 `ProblemDetail` format:

```json
{
  "status": 400,
  "detail": "Validation failed",
  "errors": {
    "amount": "Amount must be greater than zero",
    "txnDate": "Transaction date cannot be in the future"
  },
  "timestamp": "2024-03-15T10:30:00Z"
}
```

Common HTTP codes used:
- `400` Bad Request — validation errors
- `401` Unauthorized — invalid credentials
- `403` Forbidden — insufficient permissions
- `404` Not Found — resource doesn't exist or was soft-deleted
- `409` Conflict — duplicate email on user creation
- `422` Unprocessable Entity — business rule violation (e.g. invalid date range)
- `429` Too Many Requests — rate limit exceeded

---

## Rate Limiting

Each unique IP address is limited to **60 requests per minute** by default. Adjust in `application.yml`:

```yaml
rate-limit:
  capacity: 60
  refill-per-minute: 60
```

---

## Running Tests

Tests use Testcontainers, so Docker must be running.

```bash
mvn test
```

The test suite includes:
- **Unit tests** — service layer with Mockito (UserServiceTest, TransactionServiceTest)
- **Integration tests** — full HTTP stack with MockMvc and a real Postgres container (AuthControllerIntegrationTest, TransactionControllerIntegrationTest, UserControllerIntegrationTest, DashboardControllerIntegrationTest)

---

## Assumptions & Design Decisions

**Soft deletes everywhere** — Both users and transactions use `deleted_at` timestamps instead of hard deletes. Hibernate's `@SQLRestriction("deleted_at IS NULL")` automatically filters these out in all queries. This preserves audit history without needing a separate archive table.

**Entities never returned directly** — All responses go through dedicated DTO classes (`UserResponse`, `TransactionResponse`, etc.). This gives full control over what fields are exposed and prevents leaking internal state like password hashes.

**RFC 9457 ProblemDetail** — Spring Boot 3's built-in `ProblemDetail` type is used for all error responses. It's a standard format that most API clients already understand and avoids defining a custom error envelope.

**Role enforcement at the route level** — Access rules are declared in `SecurityConfig` rather than scattered across individual controller methods. This makes the permission model easier to audit at a glance.

**Partial updates via optional fields** — PUT requests accept DTOs where all fields are optional. Only non-null values get applied to the entity. This avoids the need for a separate PATCH endpoint while still keeping request bodies clean.

**No global CORS config** — Left intentionally unconfigured to keep the scope focused on backend logic. In a real deployment, this would be added in `SecurityConfig` based on the frontend origin.

**In-memory rate limiting** — Bucket4j with a `ConcurrentHashMap` handles rate limiting per client IP. This works for a single-instance deployment. For a distributed setup, this would need to be backed by Redis.

**JWT secret in config** — The JWT secret is in `application.yml` for simplicity. In production this should come from an environment variable or a secrets manager.

**Passwords in seed SQL** — The bcrypt hash in `V2__seed_users.sql` represents `password123` with cost 12. In a real project the seed would either reference environment variables or be handled by a one-time setup script.
