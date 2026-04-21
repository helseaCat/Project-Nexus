# Project-Nexus

**Multi-Tenant SaaS Data Collaboration Platform** for Aerospace Engineering Teams

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-blue)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-blue)](https://react.dev)

---

## 🎯 Overview

**Project-Nexus** is a modern **multi-tenant SaaS platform** that bridges upstream data producers and downstream consumers in complex engineering environments (inspired by aerospace industry workflows).

It features a **clean architectural separation** between three core layers:
- **Data Contracts** — Upstream-owned governance and validation layer
- **Goal Alignment & Monitoring** — Downstream-defined expectations and intelligent monitoring
- **Tasks & Workflows** — AI-powered collaboration and proactive issue resolution

When deviations occur (even subtle ones), the system uses AI to detect them and automatically suggest or create actionable tasks — turning data into aligned action.

This project demonstrates enterprise-grade patterns highly relevant to **Software Engineer II** roles in aerospace and similar industries: multi-tenancy, clean architecture, domain-driven design, real-time collaboration, and AI integration in safety-critical domains.

---

## ✨ Key Features (MVP)

### Data Contracts Layer (Upstream Owned)
- Self-service creation and versioning of Data Contracts
- Define business goals, test variables, acceptable ranges, and quality rules
- Published contracts serve as the **single source of truth** for validation

### Goal Alignment & Monitoring (Downstream Owned)
- Downstream teams define **Alignment Expectations** against published contracts
- Real-time monitoring of every payload against both the contract **and** all active expectations
- Severity levels (Warning / Critical)

### Tasks & Workflows
- Kanban-style task boards for test campaigns and investigations
- Tasks automatically linked to Data Contracts, Payloads, or detected Deviations
- AI-powered task suggestion when anomalies are detected
- Real-time collaboration via WebSocket

### Cross-Cutting Capabilities
- Strict **multi-tenancy** with Row Level Security
- JWT authentication + RBAC
- Async payload ingestion with RabbitMQ / AWS SQS
- AI deviation detection and insight generation (Spring AI + Groq/OpenAI)
- Audit trail and full traceability

---

## 🏗️ Architecture

**Core Design Principles**:
- Clean Architecture + Domain-Driven Design (DDD) bounded contexts
- Strict separation between Data Contracts, Goal Alignment, and Tasks layers
- Production-grade multi-tenancy (tenant_id + PostgreSQL RLS)
- Event-driven async processing
- Context-aware AI features

**High-Level Components**:
- **Frontend**: React 19 + TypeScript + Tailwind CSS + shadcn/ui + TanStack Query
- **Backend**: Java 21 + Spring Boot 3 (Spring Data JPA, Spring Security, Spring WebSocket, Spring AI)
- **Database**: PostgreSQL 16 with Row Level Security + Redis
- **Messaging**: RabbitMQ or AWS SQS
- **Storage**: AWS S3 for payload files
- **Deployment**: Docker + AWS ECS Fargate + Terraform / AWS CDK

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for detailed architecture, data model, and data flows.

---

## 📁 Project Structure

```
project-nexus/
├── nexus-backend/                  # Multi-module Spring Boot backend
│   ├── nexus-common/               # Shared entities, security, utilities
│   ├── nexus-contracts/            # Data Contracts bounded context
│   ├── nexus-alignment/            # Goal Alignment & Monitoring
│   ├── nexus-tasks/                # Tasks & Workflows
│   ├── nexus-ingestion/            # Async ingestion pipeline
│   └── nexus-api/                  # REST + WebSocket controllers
├── nexus-frontend/                 # React 19 + TypeScript SPA
├── docs/                           # Documentation
│   ├── BRD.md
│   ├── ARCHITECTURE.md
│   └── ...
├── infrastructure/                 # Terraform / AWS CDK
└── docker-compose.yml
```

---

## 🚀 Quick Start

### Prerequisites
- Java 21
- Node.js 20+
- Docker & Docker Compose
- PostgreSQL + Redis (or use `docker-compose`)

### Backend
```bash
cd nexus-backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd nexus-frontend
npm install
npm run dev
```

### Full Stack with Docker
```bash
docker-compose up --build
```

See [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md) (coming soon) for detailed setup.

---

## 🛠️ Tech Stack

| Layer          | Technology                                      |
|----------------|-------------------------------------------------|
| Frontend       | React 19, TypeScript, Tailwind, shadcn/ui, TanStack Query |
| Backend        | Java 21, Spring Boot 3, Spring Data JPA, Spring Security |
| Database       | PostgreSQL (RLS), Redis                         |
| Async          | RabbitMQ / AWS SQS                              |
| AI             | Spring AI + Groq / OpenAI                       |
| Cloud          | AWS (ECS Fargate, S3, RDS), Terraform / CDK     |
| Other          | Docker, JWT, OpenAPI, WebSocket                 |

---

## 📋 MVP Success Criteria

- [ ] Upstream teams can create & publish Data Contracts
- [ ] Downstream teams can define Alignment Expectations
- [ ] Payload ingestion with contract + expectation validation
- [ ] AI-powered deviation detection and task suggestion
- [ ] Strict multi-tenancy with Row Level Security
- [ ] Clean separation of concerns across layers

---

## 🎯 Why This Project Matters

This repository showcases:
- **Enterprise SaaS patterns** suitable for mission-critical aerospace systems
- **Multi-tenancy** and strict data isolation (critical for cross-team / cross-program work)
- **Clean architecture** with clear domain boundaries
- **AI integration** that augments engineering workflows rather than replacing them
- **Auditability and traceability** essential in regulated environments

---

## 📄 Documentation

- [`BRD.md`](docs/BRD.md) — Business Requirements Document
- [`ARCHITECTURE.md`](docs/ARCHITECTURE.md) — High-Level Architecture

---

## 🤝 Contributing

This is a **portfolio project** built to demonstrate senior-level full-stack engineering skills. Contributions are welcome for learning purposes.

## 📬 Contact

Built by Chelsea Scott as a portfolio piece targeting Software Engineer II roles at aerospace and tech companies.

---

**⭐ Star this repo if you find it useful for your own learning or portfolio!**

---

*Last updated: April 20, 2026*
