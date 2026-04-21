# HireConnect Backend — Microservices Architecture

**HireConnect** is a production-grade job portal backend built with **Spring Boot 3** microservices, designed to connect Recruiters and Candidates across the full hiring lifecycle.

---

## 🏗️ Architecture Overview

```
                        ┌─────────────────────────┐
                        │      API Gateway :8080   │
                        │  (JWT Filter + Routing)  │
                        └────────────┬────────────┘
              ┌─────────┬────────────┼───────────────┬──────────────┐
              │         │            │               │              │
         auth(8081) profile(8082) job(8083) application(8084) interview(8085)
                                                 │
                              notification(8086) subscription(8087) analytics(8088)
                                    │                    │
                               [RabbitMQ]            [MySQL]
```

## 📦 Microservices

| Service | Port | Database | Description |
|---|---|---|---|
| `api-gateway` | **8080** | — | JWT auth filter, route all traffic |
| `auth-service` | 8081 | `hireconnect_auth` | Registration, login, JWT, GitHub OAuth2 |
| `profile-service` | 8082 | `hireconnect_profile` | Candidate & Recruiter profiles |
| `job-service` | 8083 | `hireconnect_jobs` | Job postings, search, filter |
| `application-service` | 8084 | `hireconnect_applications` | Applications & hiring pipeline |
| `interview-service` | 8085 | `hireconnect_interviews` | Interview scheduling |
| `notification-service` | 8086 | `hireconnect_notifications` | In-app & email notifications |
| `subscription-service` | 8087 | `hireconnect_subscriptions` | Recruiter plans & invoices |
| `analytics-service` | 8088 | — | Metrics & reporting (aggregates) |

## 🛠️ Technology Stack

- **Backend:** Java 17 + Spring Boot 3.2.5
- **Security:** Spring Security + JWT (JJWT 0.11.5) + OAuth2 (GitHub)
- **Database:** MySQL 8 (one DB per service)
- **Messaging:** RabbitMQ (async notifications)
- **Caching:** Redis
- **API Gateway:** Spring Cloud Gateway 2023
- **Docs:** Swagger / OpenAPI 3.0 (per service at `/swagger-ui.html`)
- **Containerization:** Docker + Docker Compose

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- MySQL 8 (or use Docker)

### 1. Clone & build all services

```bash
git clone <repo-url>
cd hireconnect-backend
mvn clean package -DskipTests
```

### 2. Run with Docker Compose (recommended)

```bash
docker-compose up --build
```

All services will start automatically with infrastructure (MySQL, RabbitMQ, Redis).

### 3. Run individually (local dev)

Start MySQL locally, then:

```bash
# Start each service (in separate terminals)
cd auth-service && mvn spring-boot:run
cd profile-service && mvn spring-boot:run
cd job-service && mvn spring-boot:run
cd application-service && mvn spring-boot:run
cd interview-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd subscription-service && mvn spring-boot:run
cd analytics-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

---

## 🔑 API Endpoints (via Gateway on port 8080)

### Auth Service (`/api/v1/auth`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login (returns JWT) |
| POST | `/api/v1/auth/logout` | Logout |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| GET | `/api/v1/auth/validate` | Validate token |

### Job Service (`/api/v1/jobs`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/jobs` | List all active jobs |
| GET | `/api/v1/jobs/search` | Search with filters |
| POST | `/api/v1/jobs` | Post new job (Recruiter) |
| PUT | `/api/v1/jobs/{id}` | Update job |
| PATCH | `/api/v1/jobs/{id}/status` | Change status (ACTIVE/PAUSED/CLOSED) |
| DELETE | `/api/v1/jobs/{id}` | Delete job |

### Application Service (`/api/v1/applications`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/applications` | Submit application (Candidate) |
| GET | `/api/v1/applications/candidate/{id}` | My applications |
| GET | `/api/v1/applications/job/{id}` | Applications for a job |
| PATCH | `/api/v1/applications/{id}/status` | Update status (Recruiter) |
| PATCH | `/api/v1/applications/{id}/withdraw` | Withdraw (Candidate) |

### Interview Service (`/api/v1/interviews`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/interviews` | Schedule interview |
| PATCH | `/api/v1/interviews/{id}/confirm` | Confirm interview |
| PATCH | `/api/v1/interviews/{id}/reschedule` | Reschedule |
| PATCH | `/api/v1/interviews/{id}/cancel` | Cancel |

---

## 🔐 Application Status Flow

```
APPLIED → SHORTLISTED → INTERVIEW_SCHEDULED → OFFERED / REJECTED
                   └──────────────────────────────→ WITHDRAWN
```

## 💳 Subscription Plans

| Plan | Price | Job Posts | Features |
|---|---|---|---|
| FREE | ₹0 | 2/month | Basic analytics |
| PROFESSIONAL | ₹2,999/mo | Unlimited | Full analytics, resume download |
| ENTERPRISE | ₹24,999/yr | Unlimited | Team collaboration, priority support |

---

## ⚙️ Environment Variables

| Variable | Default | Description |
|---|---|---|
| `JWT_SECRET` | (base64 key) | Shared JWT signing secret |
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `root` | MySQL password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `MAIL_USERNAME` | — | SMTP email address |
| `MAIL_PASSWORD` | — | SMTP app password |
| `GITHUB_CLIENT_ID` | — | GitHub OAuth2 client ID |
| `GITHUB_CLIENT_SECRET` | — | GitHub OAuth2 client secret |

---

## 📖 Swagger Docs

Each service exposes Swagger UI at: `http://localhost:{PORT}/swagger-ui.html`

- Auth: http://localhost:8081/swagger-ui.html
- Profile: http://localhost:8082/swagger-ui.html
- Jobs: http://localhost:8083/swagger-ui.html
- Applications: http://localhost:8084/swagger-ui.html
- Interviews: http://localhost:8085/swagger-ui.html
- Notifications: http://localhost:8086/swagger-ui.html
- Subscriptions: http://localhost:8087/swagger-ui.html
- Analytics: http://localhost:8088/swagger-ui.html

---

## 🐰 RabbitMQ Management UI

- URL: http://localhost:15672
- Login: `guest` / `guest`

---

*HireConnect © 2026 | Confidential | Internal Use Only*
