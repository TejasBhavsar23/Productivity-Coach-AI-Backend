# 🧠 AI Productivity Coach — Backend

Production-ready Spring Boot backend for an AI-powered productivity coaching application.
Built with clean layered architecture, JWT security, PostgreSQL, and Claude AI integration.

---

## 📦 Tech Stack

| Layer         | Technology                              |
|---------------|-----------------------------------------|
| Framework     | Spring Boot 3.2.0                       |
| Language      | Java 17                                 |
| Security      | Spring Security + JWT (JJWT 0.11.5)    |
| Database      | PostgreSQL + Spring Data JPA / Hibernate|
| AI Integration| Claude API (Anthropic) via WebClient    |
| Documentation | Swagger UI / SpringDoc OpenAPI 3.0      |
| Build Tool    | Maven                                   |
| Testing       | JUnit 5 + H2 (in-memory for tests)      |

> ⚠️ **No Lombok** — all getters, setters, and constructors are written explicitly.

---

## 📂 Project Structure

```
src/main/java/com/productivitycoach/
├── AiProductivityCoachApplication.java   ← Entry point
│
├── controller/
│   ├── AuthController.java               ← POST /auth/signup, /auth/login
│   ├── GoalController.java               ← CRUD /goals
│   ├── DailyLogController.java           ← CRUD /logs
│   └── AiController.java                 ← /ai/analyze/day, /ai/report/weekly, /ai/chat
│
├── service/
│   ├── AuthService.java                  ← Signup / login logic
│   ├── GoalService.java                  ← Goal CRUD + PRIMARY enforcement
│   ├── DailyLogService.java              ← DailyLog + TimeEntry CRUD
│   ├── AiAnalysisService.java            ← Orchestrates all AI features
│   └── TokenUsageService.java            ← Persists AI token usage records
│
├── repository/
│   ├── UserRepository.java
│   ├── GoalRepository.java
│   ├── DailyLogRepository.java
│   ├── TimeEntryRepository.java
│   └── TokenUsageRepository.java
│
├── entity/
│   ├── User.java
│   ├── Role.java                         ← Enum: USER, ADMIN
│   ├── Goal.java
│   ├── Priority.java                     ← Enum: PRIMARY, SECONDARY
│   ├── DailyLog.java
│   ├── TimeEntry.java
│   └── TokenUsage.java
│
├── dto/
│   ├── request/
│   │   ├── SignupRequest.java
│   │   ├── LoginRequest.java
│   │   ├── GoalRequest.java
│   │   ├── DailyLogRequest.java
│   │   ├── TimeEntryRequest.java
│   │   ├── AnalyzeDayRequest.java
│   │   └── ChatRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── GoalResponse.java
│       ├── DailyLogResponse.java
│       ├── TimeEntryResponse.java
│       ├── AiResponse.java               ← Includes token usage fields
│       ├── WeeklyReportResponse.java     ← Includes token usage fields
│       └── ErrorResponse.java
│
├── security/
│   ├── JwtUtils.java                     ← Token generation & validation
│   ├── JwtAuthenticationFilter.java      ← Intercepts every request
│   ├── JwtAuthEntryPoint.java            ← Returns 401 JSON for unauth access
│   ├── UserDetailsImpl.java              ← Wraps User for Spring Security
│   ├── UserDetailsServiceImpl.java       ← Loads user by email from DB
│   └── SecurityUtils.java                ← getCurrentUserId() helper
│
├── config/
│   ├── SecurityConfig.java               ← Filter chain, CORS, route config
│   ├── WebClientConfig.java              ← Reactive HTTP client with timeouts
│   └── OpenApiConfig.java                ← Swagger + BearerAuth scheme
│
├── ai/
│   ├── AiService.java                    ← WebClient calls to Claude API
│   ├── AiApiResponse.java                ← Internal parse result with token counts
│   └── PromptTemplates.java              ← All prompt builders in one place
│
└── exception/
    ├── ResourceNotFoundException.java    ← 404
    ├── BadRequestException.java          ← 400
    ├── DuplicateResourceException.java   ← 409
    ├── AiServiceException.java           ← 502
    └── GlobalExceptionHandler.java       ← @RestControllerAdvice catch-all
```

---

## 🛢️ Entity Relationships

```
User (1) ──────────────── (N) Goal
User (1) ──────────────── (N) DailyLog
DailyLog (1) ──────────── (N) TimeEntry
User (1) ──────────────── (N) TokenUsage
```

---

## ⚙️ Prerequisites

- **Java 17+** — `java -version`
- **Maven 3.8+** — `mvn -version`
- **PostgreSQL 14+** — running locally or via Docker
- **Claude API Key** from [console.anthropic.com](https://console.anthropic.com)

---

## 🚀 Steps to Run Locally

### Step 1 — Clone / place the project

```bash
cd ai-productivity-coach
```

### Step 2 — Create the PostgreSQL database

```sql
-- Connect to psql as superuser
CREATE DATABASE productivity_coach_db;
CREATE USER productivity_user WITH ENCRYPTED PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE productivity_coach_db TO productivity_user;
```

Or with Docker (quickest):
```bash
docker run --name productivity-postgres \
  -e POSTGRES_DB=productivity_coach_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=your_password \
  -p 5432:5432 \
  -d postgres:16
```

### Step 3 — Configure application.properties

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/productivity_coach_db
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT — CHANGE THIS in production (min 256-bit / 32 chars)
app.jwt.secret=ThisIsAVeryLongAndSecureSecretKeyForJWTTokenGenerationInProduction

# Claude AI
ai.claude.api-key=sk-ant-api03-YOUR_KEY_HERE
```

### Step 4 — Build the project

```bash
mvn clean install -DskipTests
```

### Step 5 — Run the application

```bash
mvn spring-boot:run
```

Or with the JAR:
```bash
java -jar target/ai-productivity-coach-1.0.0.jar
```

### Step 6 — Verify it's running

```
GET http://localhost:8080/api/v1/swagger-ui.html
```

You should see the Swagger UI with all endpoints listed.

---

## 📡 API Endpoints

| Method | Path                    | Auth | Description                          |
|--------|-------------------------|------|--------------------------------------|
| POST   | /auth/signup            | ❌   | Register new account                 |
| POST   | /auth/login             | ❌   | Login, get JWT token                 |
| POST   | /goals                  | ✅   | Create goal (1 PRIMARY max)          |
| GET    | /goals                  | ✅   | List all goals                       |
| PUT    | /goals/{id}             | ✅   | Update goal                          |
| DELETE | /goals/{id}             | ✅   | Delete goal                          |
| POST   | /logs                   | ✅   | Create daily log with time entries   |
| GET    | /logs/{date}            | ✅   | Get log by date (yyyy-MM-dd)         |
| PUT    | /logs/{id}              | ✅   | Update log and entries               |
| DELETE | /logs/{id}              | ✅   | Delete log                           |
| POST   | /ai/analyze/day         | ✅   | AI analysis of a day                 |
| GET    | /ai/report/weekly       | ✅   | AI weekly productivity report        |
| POST   | /ai/chat                | ✅   | Chat with AI coach                   |

---

## 🔐 Authentication Flow

```
1. POST /auth/signup  →  Returns: { token, userId, email, ... }
2. Copy the token
3. All protected requests: Header → Authorization: Bearer <token>
```

---

## 🧠 AI Token Usage Tracking

Every AI endpoint response includes:

```json
{
  "responseText": "Your analysis here...",
  "promptTokens": 312,
  "completionTokens": 180,
  "totalTokens": 492
}
```

Token usage is also persisted to the `token_usages` table for analytics.

---

## 🧪 Running Tests

```bash
# Run all tests (uses H2 in-memory DB — no PostgreSQL required)
mvn test

# Run with coverage
mvn test jacoco:report
```

---

## 📬 Postman Collection

Import `postman-collection.json` into Postman:

1. Open Postman → Import → Upload File
2. Select `postman-collection.json`
3. Run **Signup** first (auto-saves token to collection variable)
4. All subsequent requests use `{{token}}` automatically

---

## 🛡️ Security Notes

- Passwords are hashed with **BCrypt** (10 rounds)
- JWT uses **HMAC-SHA256** signature
- All SQL queries go through **JPA parameterised queries** (no injection risk)
- CSRF is disabled (safe for stateless REST + JWT)
- Sessions are **STATELESS** — no server-side session storage

---

## 🏗️ Key Design Decisions

| Decision | Rationale |
|---|---|
| No Lombok | Explicit, debuggable, IDE-independent code |
| WebClient over RestTemplate | Non-blocking, production-recommended for new projects |
| @PrePersist / @PreUpdate | Consistent audit timestamps without AuditingEntityListener overhead |
| Token usage saved after response | Failure to save never breaks the AI response itself |
| Prompt templates in one class | Easier to version, review, and tune prompts independently |
| H2 for test scope | CI/CD runs without needing a live PostgreSQL instance |

---

## 🚨 Production Checklist

- [ ] Replace `app.jwt.secret` with a strong random 32+ char secret
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (not `update`) in production
- [ ] Store API keys in environment variables or a secrets manager (not properties files)
- [ ] Enable HTTPS / TLS termination at the load balancer
- [ ] Configure rate limiting on AI endpoints to control costs
- [ ] Set up log aggregation (ELK, Datadog, etc.) — SLF4J is already wired throughout
- [ ] Monitor `token_usages` table for cost tracking per user
