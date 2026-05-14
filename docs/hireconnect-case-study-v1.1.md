# HireConnect Case Study

**Job Portal with Recruiter and Candidate Dashboard**  
Version 1.1 (Corrected and Consolidated)  
Date: 2026-04-21  
Confidential - Internal Use Only

## 1. Synopsis
HireConnect is a microservices-based online job portal that connects recruiters with candidates across the full hiring lifecycle. The platform supports two primary business roles:
- **Candidate**: discovers jobs, applies, and tracks outcomes.
- **Recruiter**: publishes jobs, reviews applicants, and drives hiring decisions.

The backend provides secure authentication, profile management, job search, application tracking, interview scheduling, notifications, subscriptions, and analytics.

## 2. Business Goals
- Reduce hiring cycle time by centralizing hiring workflow.
- Improve candidate experience with transparent status tracking.
- Provide recruiters measurable pipeline analytics.
- Support a freemium monetization model for recruiters.

## 3. Scope
### In Scope
- Public job browsing and search.
- Candidate and recruiter authentication (email/password and GitHub OAuth).
- Role-based access control.
- Job posting and application workflows.
- Interview scheduling and notification flows.
- Recruiter subscription and invoicing.
- Recruiter and admin analytics endpoints.

### Out of Scope (Current Release)
- AI ranking/recommendation engine.
- Offer-letter generation workflow.
- Native mobile apps.

## 4. Actors and Permissions
| Actor | Description | Primary Permissions |
|---|---|---|
| Guest User | Unauthenticated visitor | Browse/search jobs |
| Candidate | Registered job seeker | Manage profile, apply, track status, manage interviews |
| Recruiter | Employer/HR user | Manage company profile, post jobs, manage pipeline, schedule interviews |
| Admin | Platform operations user | User moderation, platform analytics, subscription oversight |
| System | Automated internal processor | Event handling, notifications, scheduled tasks |

## 5. Functional Requirements
### 5.1 Common
- Users can browse and search jobs without login.
- Login/registration required for protected actions (apply/post/manage).
- RBAC enforced for candidate, recruiter, and admin actions.
- Users can update profile data at any time.
- System sends in-app and email notifications on key workflow events.

### 5.2 Candidate
- Create and maintain a profile with skills, experience, and resume URL.
- Search/filter jobs by title, location, salary, experience, and category.
- Apply with stored profile details and optional cover letter.
- Track application stages: `APPLIED`, `SHORTLISTED`, `INTERVIEW_SCHEDULED`, `OFFERED`, `REJECTED`, `WITHDRAWN`.
- Confirm, cancel, or request interview reschedule.
- Bookmark jobs and view historical application outcomes.

### 5.3 Recruiter
- Create and manage recruiter/company profile.
- Create, edit, pause, close, and delete job posts.
- Filter incoming applicants per job.
- Advance or reject candidates in the hiring pipeline.
- Schedule interviews and notify candidates.
- Access job and pipeline analytics.
- Manage team member access (planned extension).

### 5.4 Notifications and Messaging
- Notification triggers include status changes, interview updates, and relevant alerts.
- In-app notifications support read/unread states and bulk mark-as-read.
- Recruiter-candidate messaging is available via the notification domain.

### 5.5 Subscriptions and Billing
- Candidates use the platform at no cost.
- Recruiters can use `FREE`, `PROFESSIONAL`, and `ENTERPRISE` plans.
- Subscription lifecycle supports subscribe, renew, and cancel.
- Invoices are generated and retrievable from billing history.

## 6. Use Case Summary
| Use Case | Actors | Outcome |
|---|---|---|
| Register / Login | Candidate, Recruiter | Access secured features |
| Search Jobs | Guest, Candidate | View relevant openings |
| Apply for Job | Candidate | Application created |
| Track Application | Candidate | Real-time status visibility |
| Post Job | Recruiter | New opening published |
| Manage Applications | Recruiter | Candidate pipeline movement |
| Schedule Interview | Recruiter, Candidate | Interview lifecycle managed |
| Send Notification | System | Event-driven user updates |
| View Analytics | Recruiter, Admin | Metrics and reporting |
| Manage Subscription | Recruiter | Plan and invoice control |

## 7. Microservices Architecture
HireConnect follows a domain-aligned microservices model with synchronous REST and asynchronous messaging.

| Service | Base Package | Domain |
|---|---|---|
| auth-service | `com.hireconnect.auth` | Authentication, JWT, OAuth |
| profile-service | `com.hireconnect.profile` | Candidate/recruiter profiles |
| job-service | `com.hireconnect.job` | Job posting and discovery |
| application-service | `com.hireconnect.application` | Application workflow |
| interview-service | `com.hireconnect.interview` | Interview scheduling |
| notification-service | `com.hireconnect.notification` | Notifications and messaging |
| subscription-service | `com.hireconnect.subscription` | Plans and invoices |
| analytics-service | `com.hireconnect.analytics` | Recruiter/admin metrics |
| api-gateway | `com.hireconnect.gateway` | Routing and auth filtering |

## 8. Data and Workflow Notes
- Application state transitions are atomic and auditable.
- Resume files are stored as URLs; parsing can be handled asynchronously.
- Notification events are suitable for RabbitMQ publishing/consumption.
- Analytics are read-model style aggregates built from operational services.

## 9. Non-Functional Requirements
| Category | Requirement |
|---|---|
| Performance | Job search response target <= 2 seconds under high concurrency |
| Scalability | Independent scaling per service (Docker/Kubernetes) |
| Security | bcrypt password hashing, JWT expiry controls, OAuth2 login |
| Availability | Health endpoints and 99.9% service target |
| Integrity | ACID-compliant transactions for critical state changes |
| Maintainability | Versioned REST APIs under `/api/v1` |
| Usability | Dashboard support for desktop/mobile with accessibility goals |

## 10. Technology Stack
| Layer | Technology |
|---|---|
| Runtime | Java 21, Spring Boot 3.2.x |
| API | Spring MVC, OpenAPI/Swagger |
| Security | Spring Security, JWT, OAuth2 (GitHub) |
| Data | MySQL 8, Redis |
| Messaging | RabbitMQ |
| Search | Elasticsearch (planned/optional integration) |
| Deployment | Docker Compose (dev), Kubernetes (prod target) |
| CI/CD | GitHub Actions or Jenkins |

## 11. Assumptions and Constraints
- API contracts remain versioned at `/api/v1`.
- One database schema per microservice is preferred.
- External SMTP and OAuth credentials are environment-driven.
- Payment integrations may begin with mock providers before production gateways.

## 12. Risks and Mitigations
| Risk | Impact | Mitigation |
|---|---|---|
| Cross-service data inconsistency | Incorrect analytics/status views | Event-driven updates + idempotent consumers |
| Notification delivery failures | Missed user actions | Retry policies and dead-letter queues |
| Token/key misconfiguration | Authentication outages | Startup validation and secure secret management |
| Large resume uploads | Performance degradation | File size limits and asynchronous processing |

## 13. Acceptance Checklist
- [ ] Public job browse/search works without authentication.
- [ ] Candidate and recruiter role permissions enforced.
- [ ] End-to-end application pipeline is functional.
- [ ] Interview scheduling and notification events are operational.
- [ ] Subscription creation and invoice retrieval work for recruiter accounts.
- [ ] Recruiter/admin analytics endpoints return valid aggregates.

## 14. Alignment Notes (Current Repository)
- Core microservices and API gateway exist and compile successfully.
- Build verification command (`mvn -DskipTests compile`) succeeds across all modules.
- Java version baseline should be treated as **21** in docs and environment setup.

---
HireConnect - Internal Case Study Artifact (Corrected)

