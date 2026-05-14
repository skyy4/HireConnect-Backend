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

- **Backend:** Java 21 + Spring Boot 3.2.5
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
- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- MySQL 8 (or use Docker)

### Case Study Document

- Corrected and consolidated case study: `docs/hireconnect-case-study-v1.1.md`
- Frontend compatibility guide: `docs/frontend-integration.md`

### 1. Clone & build all services

```bash
git clone <repo-url>
cd hireconnect-backend
mvn clean package -DskipTests
```

### 2. Run with Docker Compose (recommended)

```bash
docker compose up --build
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

Source of truth: controller mappings in each service `*Resource` class + gateway route groups in `api-gateway/src/main/resources/application.yml`.

### Auth Service (`/api/v1/auth`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register user |
| POST | `/api/v1/auth/login` | Login and get JWT |
| POST | `/api/v1/auth/logout` | Logout current token |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| GET | `/api/v1/auth/validate?token=...` | Validate token |
| GET | `/api/v1/auth/user/{userId}` | Get user credential metadata |
| GET | `/api/v1/auth/users` | List users (admin) |
| PATCH | `/api/v1/auth/users/{userId}/status` | Set user active status (admin) |

### Profile Service (`/api/v1/profiles`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/profiles/candidates` | Create candidate profile |
| GET | `/api/v1/profiles/candidates/{profileId}` | Candidate profile by profileId |
| GET | `/api/v1/profiles/candidates/user/{userId}` | Candidate profile by auth userId |
| PUT | `/api/v1/profiles/candidates/{profileId}` | Update candidate profile |
| POST | `/api/v1/profiles/recruiters` | Create recruiter profile |
| GET | `/api/v1/profiles/recruiters/{profileId}` | Recruiter profile by profileId |
| GET | `/api/v1/profiles/recruiters/user/{userId}` | Recruiter profile by auth userId |
| PUT | `/api/v1/profiles/recruiters/{profileId}` | Update recruiter profile |
| POST | `/api/v1/profiles/candidates/{profileId}/resume/upload` | Upload resume (multipart) |
| GET | `/api/v1/profiles/recruiters/{recruiterId}/team` | List recruiter team members |

### Job Service (`/api/v1/jobs`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/jobs` | List active jobs |
| GET | `/api/v1/jobs/{jobId}` | Job details (+ optional view tracking params) |
| GET | `/api/v1/jobs/search` | Search with filters |
| GET | `/api/v1/jobs/recruiter/{recruiterId}` | Jobs posted by recruiter |
| POST | `/api/v1/jobs` | Create job |
| PUT | `/api/v1/jobs/{jobId}` | Update job |
| PATCH | `/api/v1/jobs/{jobId}/status` | Update status |
| POST | `/api/v1/jobs/bookmarks` | Save job bookmark |
| GET | `/api/v1/jobs/{jobId}/views/count` | View count |

### Application Service (`/api/v1/applications`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/applications` | Submit application |
| GET | `/api/v1/applications/{applicationId}` | Get one application |
| GET | `/api/v1/applications/candidate/{candidateId}` | Candidate applications |
| GET | `/api/v1/applications/job/{jobId}` | Job applications (`?status=` optional) |
| PATCH | `/api/v1/applications/{applicationId}/status` | Update status |
| PATCH | `/api/v1/applications/{applicationId}/withdraw?candidateId={candidateId}` | Withdraw application |
| GET | `/api/v1/applications/check?jobId={jobId}&candidateId={candidateId}` | Check already applied |

### Interview Service (`/api/v1/interviews`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/interviews` | Schedule interview |
| GET | `/api/v1/interviews/{interviewId}` | Interview details |
| GET | `/api/v1/interviews/candidate/{candidateId}` | Candidate interviews |
| GET | `/api/v1/interviews/recruiter/{recruiterId}` | Recruiter interviews |
| PATCH | `/api/v1/interviews/{interviewId}/confirm` | Confirm interview |
| PATCH | `/api/v1/interviews/{interviewId}/reschedule` | Reschedule interview |
| PATCH | `/api/v1/interviews/{interviewId}/cancel` | Cancel interview |

### Notifications + Messages (`/api/v1/notifications`, `/api/v1/messages`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/notifications/user/{userId}` | User notifications |
| PATCH | `/api/v1/notifications/{notificationId}/read` | Mark notification read |
| POST | `/api/v1/messages` | Send message |
| GET | `/api/v1/messages/conversation?userId1=...&userId2=...` | Conversation thread |
| GET | `/api/v1/messages/inbox/{userId}` | Inbox |

### Subscription + Billing (`/api/v1/subscriptions`, `/api/v1/invoices`, `/api/v1/wallet`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/subscriptions` | Subscribe recruiter |
| GET | `/api/v1/subscriptions/recruiter/{recruiterId}/active` | Active plan |
| PATCH | `/api/v1/subscriptions/recruiter/{recruiterId}/renew` | Renew/upgrade |
| GET | `/api/v1/invoices/recruiter/{recruiterId}` | Recruiter invoices |
| GET | `/api/v1/wallet/{userId}/balance` | Wallet balance |

### Analytics (`/api/v1/analytics`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/analytics/recruiter/{recruiterId}` | Recruiter dashboard metrics |
| GET | `/api/v1/analytics/job/{jobId}` | Job-level analytics |
| GET | `/api/v1/analytics/admin` | Platform analytics |

### Identifier semantics
- `userId`: identity from auth-service/JWT claims.
- `profileId`: profile-service record id (candidate/recruiter profile).
- `candidateId` / `recruiterId`: domain actor id used by application/interview/job flows.

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
