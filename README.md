![CI](https://github.com/PauLopNun/banking-api/actions/workflows/ci.yml/badge.svg)

# 🏦 Banking API

A production-ready REST API for banking operations built with **Spring Boot 3.5** and **Java 21**.  
Developed as part of the **GFT Junior Training Programme 2026**.

---

## ✨ Features

- 🔐 **JWT Authentication** — secure register & login
- 💳 **Account Management** — create, query and delete bank accounts
- 💸 **Transfers** — transactional money transfers with rollback protection
- 📜 **Transaction History** — paginated audit trail per account
- ✅ **Input Validation** — clear error messages on bad requests
- 🧪 **19 Tests** — unit + integration tests covering all business logic
- 🐳 **Docker** — multi-stage build for lightweight production image
- 🏗️ **Hexagonal Architecture** — clean separation of domain, application, and infrastructure

---

## 🛠️ Tech Stack

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

## 📁 Project Structure

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

## 🚀 Getting Started

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

This starts both the app and a PostgreSQL database automatically.

### Run with Docker only

```bash
docker build -t banking-api .
docker run -p 8080:8080 banking-api
```

The API will start on `http://localhost:8080`

### H2 Console (dev only)
Access the in-memory database at `http://localhost:8080/h2-console`

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:bankingdb` |
| Username | `sa` |
| Password | *(empty)* |

---

## 📡 API Endpoints

### 🔐 Auth
| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | ❌ |
| POST | `/api/auth/login` | Login and get JWT token | ❌ |

### 💳 Accounts
| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| POST | `/api/accounts` | Create a new account | ✅ |
| GET | `/api/accounts` | Get all accounts | ✅ |
| GET | `/api/accounts/{id}` | Get account by ID | ✅ |
| DELETE | `/api/accounts/{id}` | Delete account (only if balance is 0) | ✅ |

### 💸 Transfers
| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| POST | `/api/transfers` | Make a transfer | ✅ |
| GET | `/api/transfers/history/{accountId}?page=0&size=10` | Get paginated account history | ✅ |

---

## 🔑 Authentication

Register and get your token:

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
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Use the token in subsequent requests:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 💸 Transfer Example

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

## 🧪 Running Tests

```bash
./mvnw test
```

| Test class | Tests | Status |
|---|---|---|
| `AccountServiceTest` | 6 | ✅ |
| `TransferServiceTest` | 7 | ✅ |
| `AccountIntegrationTest` | 5 | ✅ |
| `BankingApplicationTests` | 1 | ✅ |
| **Total** | **19** | ✅ |

---

## 🏗️ Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters):

```
HTTP Request
     ↓
Controller (api layer)
     ↓
Service (application layer)
     ↓
Repository interface (domain port)
     ↓
JPA Repository (infrastructure adapter)
     ↓
Database (H2 / PostgreSQL)
```

---

## 🔒 Security Notes

- Passwords are hashed with **BCrypt** — never stored in plain text
- JWT tokens expire after **24 hours**
- All endpoints except `/api/auth/**` require a valid Bearer token
- Sessions are **stateless** — no server-side session storage

---

## 👨‍💻 Author

**Pau López Núñez**  
Junior Backend Developer @ GFT Technologies SE  
[GitHub](https://github.com/PauLopNun)

---

*Built as part of GFT Junior Training Programme 2026 — Backend track*