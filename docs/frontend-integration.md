# Frontend Integration Guide (HireConnect Backend)

This guide defines a stable contract for connecting a React/SPA frontend to `hireconnect-backend` through the API gateway.

## Base URL
- Gateway base URL: `http://localhost:8080`
- API prefix: `/api/v1`
- Frontend origin (local dev / Docker): `http://localhost:3000`

## Auth Flow
1. `POST /api/v1/auth/login`
2. Store returned JWT on frontend.
3. Send JWT in header for protected calls:
   - `Authorization: Bearer <token>`

## ID Semantics (Important)
- `userId`: auth identity from JWT (`/api/v1/auth/*`).
- `profileId`: profile-service record id (`/api/v1/profiles/candidates/{profileId}`, `/api/v1/profiles/recruiters/{profileId}`).
- `candidateId` / `recruiterId`: hiring-domain ids used by applications/jobs/interviews.

## CORS and Browser Behavior
- Gateway CORS is enabled for browser clients.
- `OPTIONS` preflight requests are allowed.
- Unauthorized requests return JSON:
  - `{"error":"UNAUTHORIZED","message":"..."}`

## Frontend-Safe Response Keys
To reduce UI parsing issues, these endpoints now expose normalized count keys:

- `GET /api/v1/jobs/{jobId}/views/count`
  - `{ "count": 12, "viewCount": 12 }`

- `GET /api/v1/jobs/recruiter/{recruiterId}/count`
  - `{ "count": 5, "totalJobs": 5 }`

- `GET /api/v1/applications/check?jobId=...&candidateId=...`
  - `{ "applied": true, "hasApplied": true }`

## New Aggregate Endpoints
- `GET /api/v1/jobs/count?status=ACTIVE`
- `GET /api/v1/applications/count`
- `GET /api/v1/applications/job/{jobId}/count?status=SHORTLISTED`

These are used by analytics and can also power dashboard cards directly.

## Recommended Frontend Env
```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
APP_FRONTEND_URL=http://localhost:3000
```

## Minimal Dashboard Calls
Candidate dashboard:
- `GET /api/v1/jobs/search`
- `GET /api/v1/profiles/candidates/user/{userId}`
- `GET /api/v1/applications/candidate/{candidateId}`
- `GET /api/v1/notifications/user/{candidateId}`

Candidate actions:
- `PATCH /api/v1/applications/{applicationId}/withdraw?candidateId={candidateId}`

Recruiter dashboard:
- `GET /api/v1/profiles/recruiters/user/{userId}`
- `GET /api/v1/jobs/recruiter/{recruiterId}`
- `GET /api/v1/analytics/recruiter/{recruiterId}`
- `GET /api/v1/subscriptions/recruiter/{recruiterId}/active`

