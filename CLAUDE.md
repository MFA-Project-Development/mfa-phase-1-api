# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build (skip tests)
./gradlew clean build -x test

# Build with tests
./gradlew clean build

# Run application
./gradlew bootRun

# Run tests
./gradlew test

# Start dev dependencies (PostgreSQL, MinIO, Redis)
docker-compose up

# Start production environment
docker-compose -f compose.prod.yml up
```

## Architecture

This is a **Spring Boot 3.5.5 microservice** (Java 21) that manages educational assessments, classes, and student engagement. It runs on port **8003** and is part of a broader microservice ecosystem.

### Service Communication

- **Eureka** (localhost:8761 in dev): Service registration and discovery
- **Keycloak**: OAuth2 JWT authentication; all endpoints secured via `SecurityConfiguration.java` using `@PreAuthorize`
- **UserClient** (Feign): Calls the authentication/user service for user data; has circuit breaker fallbacks in `client/fallback/`
- **Socket.IO**: `SocketIoClientService` pushes real-time events to connected clients

### Domain Model

The core domain is an **assessment lifecycle**:

1. **Class** → container grouping students and instructors around a sub-subject
2. **Assessment** → quiz/exam/assignment within a class; status flow: `DRAFT → PUBLISHED → IN_PROGRESS → FINISHED`
3. **Question** → belongs to an assessment; supports multiple types (MCQ, short answer, file uploads)
4. **Answer** → a student's response to a question
5. **Submission** → wraps all answers for a student on an assessment; status flow: `PENDING → SUBMITTED → GRADED`

Supporting domains: `Subject/SubSubject` (course hierarchy), `MotivationContent` (engagement materials), `ActivityLog` (audit trail), `StudentClassEnrollment`.

### Scheduled Jobs

`AssessmentStartJob` and `AssessmentFinishJob` are Quartz jobs (JDBC job store, 10 threads) that automatically transition assessment status at scheduled times. Managed via `QuartzSchedulerService`.

### File Storage

All file uploads (PDFs, images) go through `FileService` → MinIO (S3-compatible). Configured in `MinioConfig.java`. Max upload: 20 MB per file, 30 MB per request.

### Activity Logging

Methods annotated with `@AuditAction` (in `model/annotation/`) are intercepted to write to `ActivityLog` automatically via `ActivityLogService`.

### Layer Conventions

- **Controllers** → `controller/` — REST endpoints, use `@PreAuthorize` for authorization
- **Services** → `service/` (interfaces) + `service/serviceimpl/` (implementations)
- **Repositories** → `service/repository/` — Spring Data JPA interfaces
- **Entities** → `model/entity/`; DTOs in `model/dto/request/` and `model/dto/response/`
- **Enums** → `model/enums/` — 30+ business enums covering status flows and types
- **Exceptions** → `exception/`; `GlobalException.java` is the `@ControllerAdvice` handler

### Configuration Profiles

| Profile | Keycloak | Database | Eureka |
|---------|----------|----------|--------|
| `dev` (default) | localhost:8080 | localhost:5435 | localhost:8761 |
| `prod` | keycloak.dara-it.site | env-injected | service mesh |

Environment variables for dev are in `.env` (not committed). Key vars: `POSTGRES_*`, `MINIO_*`, `KEYCLOAK_CLIENT_ID`, `KEYCLOAK_CLIENT_SECRET`.
