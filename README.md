# Task Management API

A RESTful API for task management built with **Java 21** and **Spring Boot 3.5**. It provides JWT-based authentication with refresh-token rotation and server-side revocation, Spring Security authorization, a Many-to-Many Task–Tag model, MapStruct entity–DTO mapping, Flyway migrations, and unit tests with JUnit 5 + Mockito.

<!-- After deploying, fill these in: -->
**Live demo:** https://YOUR-APP.up.railway.app
**Interactive API docs (Swagger UI):** https://YOUR-APP.up.railway.app/swagger-ui/index.html

## Tech Stack

| Layer      | Technology                              |
| ---------- | --------------------------------------- |
| Language   | Java 21                                 |
| Framework  | Spring Boot 3.5                         |
| Security   | Spring Security, JWT (jjwt 0.12.6)      |
| ORM        | Spring Data JPA / Hibernate             |
| Database   | PostgreSQL                              |
| Migration  | Flyway                                  |
| Mapping    | MapStruct                               |
| API Docs   | springdoc-openapi (Swagger UI)          |
| Testing    | JUnit 5, Mockito                        |
| Build Tool | Maven                                   |
| Utilities  | Lombok                                  |

## Features

* JWT authentication with refresh-token rotation and server-side revocation
* Spring Security authorization; each user can only access their own resources
* Task CRUD with a Many-to-Many Task–Tag relationship (JPA join table)
* Fail-fast validation (invalid tag IDs are rejected instead of silently dropped)
* Centralized Global Exception Handler with consistent HTTP status codes (401 / 403 / 404) that avoids leaking system details
* Automated Entity–DTO mapping with MapStruct
* Flyway-managed database migrations
* Unit tests for the service layer (JUnit 5 + Mockito), covering happy and unhappy paths
* Interactive API documentation via Swagger UI
* Postman collection included under `/postman`

## Project Structure

```text
src/
├── main/
│   ├── java/com/taskmanagement/task_management_api/
│   │   ├── config/          # Security config, OpenAPI/Swagger config
│   │   ├── controller/      # REST endpoints (auth, tasks, tags)
│   │   ├── filter/          # JWT authentication filter
│   │   ├── service/         # Business logic (+ impl)
│   │   ├── repository/      # JPA repositories
│   │   ├── entity/          # JPA entities
│   │   ├── mapper/          # MapStruct mappers
│   │   ├── dto/             # Request / Response DTOs
│   │   └── exception/       # Global exception handling
│   └── resources/
│       ├── application.properties
│       └── db/migration/    # Flyway SQL scripts (V1–V5)
└── test/                    # JUnit 5 + Mockito tests
```

## Getting Started

### Prerequisites

* Java 21+
* PostgreSQL
* Maven 3.8+ (or use the bundled `./mvnw`)

### 1. Clone

```bash
git clone https://github.com/NguyenLeDuyy/Task-Management-API.git
cd Task-Management-API
```

### 2. Create the database

```sql
CREATE DATABASE task_management_db;
```

### 3. Configure via environment variables

This project reads all secrets from environment variables (with local-friendly defaults for the datasource). **No credentials are committed to the repository.** Set at least the JWT secret before running:

```bash
# Generate a strong base64 secret (256-bit) and export it
export JWT_SECRET=$(openssl rand -base64 32)

# Optional — override the defaults if your local DB differs
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/task_management_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
```

| Variable                      | Required | Default (local)                                       |
| ----------------------------- | -------- | ----------------------------------------------------- |
| `JWT_SECRET`                  | yes      | —                                                     |
| `SPRING_DATASOURCE_URL`       | no       | `jdbc:postgresql://localhost:5432/task_management_db` |
| `SPRING_DATASOURCE_USERNAME`  | no       | `postgres`                                            |
| `SPRING_DATASOURCE_PASSWORD`  | no       | `postgres`                                            |
| `JWT_EXPIRATION`              | no       | `86400000` (24h)                                      |
| `JWT_REFRESH_EXPIRATION`      | no       | `604800000` (7d)                                      |
| `PORT`                        | no       | `8080`                                                |

### 4. Run

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`. Flyway applies migrations automatically on startup. Open Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

### Run the tests

```bash
./mvnw test
```

## API Documentation (Swagger)

Interactive docs are served by springdoc-openapi:

* Swagger UI: `/swagger-ui/index.html`
* OpenAPI JSON: `/v3/api-docs`

**How to call protected endpoints from Swagger:**

1. Call `POST /api/auth/register`, then `POST /api/auth/login` to get an access token.
2. Click the **Authorize** button (top right), paste the token, and confirm.
3. All `/api/tasks` and `/api/tags` endpoints can now be tried directly from the browser.

## API Endpoints

### Auth (public)

| Method | Endpoint             | Description                              |
| ------ | -------------------- | ---------------------------------------- |
| POST   | `/api/auth/register` | Register a new user                      |
| POST   | `/api/auth/login`    | Login; returns access + refresh tokens   |
| POST   | `/api/auth/refresh`  | Exchange a refresh token for a new access token |
| POST   | `/api/auth/logout`   | Revoke a refresh token (server-side)     |

### Tasks (require `Authorization: Bearer <token>`)

| Method | Endpoint          | Description                        |
| ------ | ----------------- | ---------------------------------- |
| GET    | `/api/tasks`      | Get all tasks for the current user |
| GET    | `/api/tasks/{id}` | Get one task by ID                 |
| POST   | `/api/tasks`      | Create a task (optionally with `tagIds`) |
| PUT    | `/api/tasks/{id}` | Update a task                      |
| DELETE | `/api/tasks/{id}` | Delete a task                      |

### Tags (require `Authorization: Bearer <token>`)

| Method | Endpoint         | Description        |
| ------ | ---------------- | ------------------ |
| GET    | `/api/tags`      | List tags          |
| GET    | `/api/tags/{id}` | Get one tag by ID  |
| POST   | `/api/tags`      | Create a tag       |
| PUT    | `/api/tags/{id}` | Update a tag       |
| DELETE | `/api/tags/{id}` | Delete a tag       |

## Example: full auth flow

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"duy@example.com","password":"secret123"}'

# 2. Login -> copy the accessToken from the response
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"duy@example.com","password":"secret123"}'

# 3. Create a task with the token
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Review pull request","description":"Merge feature/auth","status":"TODO","tagIds":[]}'
```

## Testing with Postman

A Postman collection is included in the `/postman` directory and covers the register/login flow, JWT authorization, task and tag CRUD, and access-control checks for user-owned resources.

## Database Migrations

Migrations are managed by **Flyway** and run automatically on startup. Scripts live in `src/main/resources/db/migration/` (`V1`–`V5`). `spring.jpa.hibernate.ddl-auto` is set to `validate`, so Hibernate verifies the schema Flyway created rather than modifying it.

## Author

**Nguyen Le Duy**
GitHub: [NguyenLeDuyy](https://github.com/NguyenLeDuyy)