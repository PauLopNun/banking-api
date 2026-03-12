# 🏦 Banking API

A production-ready REST API for banking operations built with **Spring Boot 3.5** and **Java 21**.  
Developed as part of the **GFT Junior Training Programme 2026**.

---

## ✨ Features

- 🔐 **JWT Authentication** — secure register & login
- 💳 **Account Management** — create and query bank accounts
- 💸 **Transfers** — transactional money transfers with rollback protection
- 📜 **Transaction History** — full audit trail per account
- ✅ **Input Validation** — clear error messages on bad requests
- 🧪 **Unit Tests** — 11 tests with Mockito covering all business logic
- 🏗️ **Hexagonal Architecture** — clean separation of domain, application, and infrastructure

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.11 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Persistence | Spring Data JPA + Hibernate 6 |
| Database | H2 (in-memory) |
| Validation | Jakarta Bean Validation |
| Boilerplate reduction | Lombok |
| Testing | JUnit 5 + Mockito |
| Build tool | Maven |

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

### Run the application

```bash
git clone https://github.com/PauLopNun/banking-api.git
cd banking-api
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`

### H2 Console
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

### 💸 Transfers
| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| POST | `/api/transfers` | Make a transfer | ✅ |
| GET | `/api/transfers/history/{accountId}` | Get account history | ✅ |

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

Response:
```json
{
  "id": 1,
  "fromAccountId": 1,
  "fromAccountOwner": "Pau López",
  "toAccountId": 2,
  "toAccountOwner": "Ivan Carmona",
  "amount": 200.00,
  "createdAt": "2026-03-12T12:40:52"
}
```

---

## 🧪 Running Tests

```bash
./mvnw test
```

Current test coverage:

| Test class | Tests | Status |
|---|---|---|
| `AccountServiceTest` | 4 | ✅ |
| `TransferServiceTest` | 7 | ✅ |

---

## 🏗️ Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters):

- The **domain** layer has zero dependencies on frameworks or databases
- **Services** orchestrate business logic and only depend on interfaces
- **Controllers** handle HTTP and delegate everything to services
- **Repositories** are the only layer that touches the database

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
Database
```

---

## 🔒 Security Notes

- Passwords are hashed with **BCrypt** — never stored in plain text
- JWT tokens expire after **24 hours**
- All endpoints except `/api/auth/**` require a valid Bearer token
- Sessions are **stateless** — no server-side session storage

---

## 📌 Roadmap

- [ ] Pagination for transfer history
- [ ] Integration tests with `@SpringBootTest`
- [ ] Dockerize with multi-stage build
- [ ] CI/CD pipeline with GitHub Actions

---

## 👨‍💻 Author

**Pau López Núñez**  
Junior Backend Developer @ GFT Technologies SE  
[GitHub](https://github.com/PauLopNun)

---

*Built as part of GFT Junior Training Programme 2026 — Backend track*
