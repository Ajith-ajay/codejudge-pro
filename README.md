# CodeJudge Pro — Enterprise Online Coding Assessment Platform

CodeJudge Pro is a production-grade, scalable online coding assessment and MCQ testing platform built using a clean monorepo architecture. Designed to emulate commercial grading systems like HackerRank and LeetCode, it features a sandboxed code execution engine, real-time leaderboard statistics, robust anti-cheating controls, and comprehensive exam analytics.

---

## 🛠️ Technology Stack

| Component | Technology |
| :--- | :--- |
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.x & Spring Security 6 |
| **Database** | PostgreSQL 17 (relational data & schemas) |
| **Migration** | Flyway |
| **Caching** | Redis (sorted sets for live leaderboards, session store) |
| **Monorepo Client** | Next.js (App Router, TypeScript, Tailwind CSS) |
| **Authentication** | Stateless JWT (Access/Refresh Tokens) |
| **Testing** | JUnit 5, Mockito, H2, Testcontainers |
| **Containers** | Docker & Docker Compose |
| **Mock Mail Server**| Mailpit (local verification and email mock checks) |

---

## 📂 Repository Architecture

The codebase follows a **monorepo pattern**, separating backend services, web applications, and configuration stacks cleanly:

```text
codejudge-pro/
├── apps/
│   ├── backend/        # Spring Boot Java application
│   └── web/            # Next.js React frontend web portal
├── infrastructure/
│   ├── docker/         # Dockerfiles and infrastructure service properties
│   ├── nginx/          # Nginx reverse proxy configuration
│   └── scripts/        # Automation, cleanup, and seeding utilities
├── docs/               # System architecture and API contract documentation
├── docker-compose.yml  # Local database, Redis, and Mailpit environment
├── .env.example        # Environment variables template
├── README.md           # Root configuration and run guide
└── LICENSE             # MIT license
```

---

## 🚀 Quick Start Guide

### Prerequisites
Make sure you have the following installed on your local development machine:
- **Java JDK 21**
- **Docker & Docker Compose**
- **Maven** (optional, wrapper `./mvnw` is included in the backend)
- **Node.js** (v18.x or later) and **npm**

### Step 1: Environment Configuration
Copy the sample environment file to configure ports and credentials:
```bash
cp .env.example .env
```
*(On Windows, run `copy .env.example .env` in PowerShell)*

### Step 2: Spin Up Infrastructure Containers
Start Postgres, Redis, and Mailpit in the background:
```bash
docker compose up -d
```
Verify they are running:
```bash
docker compose ps
```
- **PostgreSQL**: Port `5435`
- **Redis**: Port `6380`
- **Mailpit SMTP**: Port `1026`
- **Mailpit Web UI**: Port `8026` (Access http://localhost:8026 to see outgoing emails)

### Step 3: Run the Spring Boot Backend
Navigate to the backend directory and launch the application using the `dev` profile:
```bash
cd apps/backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
*(On Windows PowerShell, use `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev`)*

The backend will automatically start on **http://localhost:8080** and run all Flyway migrations against your PostgreSQL container.

### Step 4: Run the Next.js Frontend
Navigate to the web portal directory:
```bash
cd apps/web
npm install
npm run dev
```

---

## 🏗️ Project Development Roadmap

CodeJudge Pro is being built in structured phases to ensure clean architecture and testing reliability:

1. **Phase 1: Project Setup & Common Core** (Complete) — Configured POM properties, pagination objects, global validation error advisory, and database files.
2. **Phase 2: Database Migration Setup** (Complete) — Formed all 22 relational schemas using Flyway migrations.
3. **Phase 3: Auth & Security Module** (In Progress) — JWT validation filter, user registration, role authorization, and forgot-password mail alerts.
4. **Phase 4: Question & Exam Management** — MCQ structure formats, testcase imports, candidate invites, and schedule windows.
5. **Phase 5: Compiler Sandbox Service** — Docker runner setups to safely compile user code with execution limits.
6. **Phase 6: Submission & Grading Engine** — Code run and submission evaluations, scoring, and transaction blocks.
7. **Phase 7: Analytics & Leaderboards** — Redis sorted-set scoring, audit logging, and cheating checks.
8. **Phase 8: Frontend Skeletal Layout** — Next.js client initialization, styling sheets, candidate workspace layouts.
9. **Phase 9: Monaco Code Editor Integrations** — Live code execution workspaces and logs.
10. **Phase 10: Anti-Cheating & Reports** — Tab switching and full-screen enforcement, PDF candidate sheet reports.
