![CI](https://github.com/PauLopNun/banking-api/actions/workflows/ci.yml/badge.svg)

# Banking API

A production-ready REST API for banking operations built with **Spring Boot 3.5** and **Java 21**.  
Developed as part of the **GFT Junior Training Programme 2026**.

---

## Features

- **JWT Authentication** — secure register and login endpoints
- **Refresh Tokens** — token rotation for session renewal
- **Account Management** — create, query and delete bank accounts scoped to the authenticated user
- **Transfers** — transactional money transfers with rollback protection
- **Transaction History** — paginated audit trail per account with owner access control
- **Input Validation** — descriptive error messages on malformed requests
- **Rate Limiting** — per-IP protection (60 requests/minute) using Bucket4j
- **Swagger / OpenAPI** — interactive API documentation at runtime
- **36 Tests** — unit and integration tests covering business and security paths
- **Docker** — multi-stage build for a lightweight production image
- **Hexagonal Architecture** — clean separation of domain, application and infrastructure layers

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.11 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Persistence | Spring Data JPA + Hibernate 6 |
| Database (dev) | H2 (in-memory) |
| Database (prod) | PostgreSQL 16 |
| Profiles | dev (H2) / prod (PostgreSQL) |
| Validation | Jakarta Bean Validation |
| Boilerplate reduction | Lombok |
| Testing | JUnit 5 + Mockito + SpringBootTest |
| Build tool | Maven |
| Containerization | Docker (multi-stage) + Docker Compose |
| CI | GitHub Actions |

---

## Project Structure

```
src/main/java/com/gft/banking/
├── domain/
│   └── model/               # Account, Transfer, User entities
├── application/
│   └── service/             # AccountService, TransferService, AuthService
├── infrastructure/
│   ├── persistence/         # JPA Repositories
│   └── security/            # JWT filter, Security config, UserDetailsService
└── api/
    ├── controller/          # REST Controllers
    ├── dto/                 # Request/Response DTOs
    └── exception/           # Global exception handler
```

---

## Getting Started

### Prerequisites
- Java 21
- Maven 3.8+

### Run locally (dev profile — H2)

```bash
git clone https://github.com/PauLopNun/banking-api.git
cd banking-api
./mvnw spring-boot:run
```

### Run with Docker Compose (recommended — PostgreSQL)

```bash
docker-compose up --build
```

This starts both the application and a PostgreSQL database automatically.

### Run with Docker only

```bash
docker build -t banking-api .
docker run -p 8080:8080 banking-api
```

The API will be available at `http://localhost:8080`.

### H2 Console (dev profile only)

Access the in-memory database at `http://localhost:8080/h2-console`.

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:bankingdb` |
| Username | `sa` |
| Password | *(empty)* |

---

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | Login and receive a JWT token | No |
| POST | `/api/auth/refresh` | Rotate refresh token and obtain a new access token | No |

### Accounts

| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| POST | `/api/accounts` | Create a new account | Yes |
| GET | `/api/accounts` | List all accounts | Yes |
| GET | `/api/accounts/{id}` | Get account by ID | Yes |
| DELETE | `/api/accounts/{id}` | Delete account (only if balance is 0) | Yes |

### Transfers

| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| POST | `/api/transfers` | Execute a transfer | Yes |
| GET | `/api/transfers/history/{accountId}?page=0&size=10` | Get paginated transaction history | Yes |

---

## Authentication

Register and obtain a token:

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "pau",
  "password": "1234"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "9f96f4a4-..."
}
```

Include the token in subsequent requests:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Transfer Example

```http
POST /api/transfers
Authorization: Bearer <token>
Content-Type: application/json

{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 200.00
}
```

---

## Running Tests

```bash
./mvnw test
```

| Test class | Tests |
|---|---|
| `AccountServiceTest` | 6 |
| `TransferServiceTest` | 8 |
| `AccountIntegrationTest` | 6 |
| `TransferIntegrationTest` | 7 |
| `RefreshTokenServiceTest` | 4 |
| `RateLimitInterceptorTest` | 1 |
| `AuthIntegrationTest` | 3 |
| `BankingApplicationTests` | 1 |
| **Total** | **36** |

## API Documentation

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters):

```
HTTP Request
     |
Controller (api layer)
     |
Service (application layer)
     |
Repository interface (domain port)
     |
JPA Repository (infrastructure adapter)
     |
Database (H2 / PostgreSQL)
```

---

## Security

- Passwords are hashed with **BCrypt** and never stored in plain text.
- JWT tokens expire after **24 hours**.
- Refresh tokens are rotated and invalidated on each refresh operation.
- Rate limiting applies to `/api/**` with per-IP buckets.
- All endpoints except `/api/auth/**` require a valid Bearer token.
- Sessions are **stateless** — no server-side session storage.

---

## Author

**Pau López Núñez**  
Junior Backend Developer @ GFT Technologies SE  
[GitHub](https://github.com/PauLopNun)

---

*Built as part of the GFT Junior Training Programme 2026 — Backend track.*
