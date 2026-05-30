# SurvivorOutDraft — Spring Boot Backend

REST API and business logic layer for the SurvivorOutDraft fantasy league app. Built with Spring Boot 4, PostgreSQL, Flyway, and JWT authentication.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.1 (Java 21) |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Migrations | Flyway |
| Auth | Spring Security + JWT (JJWT 0.12.3) |
| Build | Maven |
| Utilities | Lombok, Jackson |

---

## Prerequisites

- Java 21
- PostgreSQL running locally on port 5432
- Database `soddb` created
- Maven (or use `./mvnw`)

---

## Configuration

Copy or edit `src/main/resources/application.properties`. Key properties:

```properties
# Server
server.port=${PORT:8080}

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/soddb
spring.datasource.username=<username>
spring.datasource.password=<password>

# JWT
vivida.jwt.secret=${JWT_SECRET}           # Required — set via env var
vivida.jwt.expiration=86400000            # 24 hours in ms

# CORS
vivida.cors.allowed-origin-patterns=${VIVIDA_CORS_ALLOWED_ORIGIN_PATTERNS:*}

# DataLoader feature flags
vivida.dataloader.game-data=true          # Load Survivor game data on startup
vivida.dataloader.test-data=true          # Seed test users/groups
vivida.dataloader.incremental=false       # Delta load (skip wipe)
```

Required environment variables:

```bash
export JWT_SECRET=<your-secret-key>
```

---

## Running Locally

```bash
cd spring-boot
./mvnw spring-boot:run
```

Or build and run the jar:

```bash
./mvnw clean package -DskipTests
java -jar target/*.jar
```

---

## Project Structure

```
src/main/java/com/vivida/
├── Application.java
├── auth/                  # JWT filter, auth service, login/register
├── config/
│   ├── DataLoader.java    # Startup data seeding from JSON files
│   └── SecurityConfig.java
├── draft/                 # Draft orchestration (picks, snake order, status)
├── game/
│   ├── advantage/         # Advantage movements (idols, powers)
│   ├── boot/              # Eliminations
│   ├── castaway/          # Contestants and season performances
│   ├── challenge/         # Challenges and results
│   ├── episode/           # Season episodes
│   ├── journey/           # Island journeys
│   ├── juryVote/          # Final jury votes
│   ├── season/            # Survivor seasons
│   ├── tribal/            # Tribal councils
│   ├── tribe/             # Tribes
│   └── vote/              # Votes and vote rounds
├── scoring/               # Point rules and score calculation
├── social/
│   ├── group/             # Draft leagues
│   └── team/              # User teams within groups
└── common/                # Shared utilities
```

---

## API Overview

Base path: `/api/v1`

### Auth

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login — returns JWT token |

### Users

| Method | Path | Description |
|---|---|---|
| GET | `/users/{id}` | Get user by ID |
| GET | `/users/username/{username}` | Get user by username |
| POST | `/users/{id}/avatar` | Upload avatar image |
| PUT | `/users/` | Update user |

### Groups

| Method | Path | Description |
|---|---|---|
| GET | `/groups/` | List all groups |
| GET | `/groups/{id}` | Get group |
| GET | `/groups/{id}/dashboard` | Group dashboard with members, teams, scores |

### Drafts

| Method | Path | Description |
|---|---|---|
| GET | `/drafts/{draftId}` | Get draft state |
| GET | `/drafts/group/{groupId}` | Get active draft for a group |
| POST | `/drafts/{draftId}/start` | Start draft |
| POST | `/drafts/{draftId}/pick` | Make a pick `{castawayPerformanceId}` |
| POST | `/drafts/{draftId}/complete` | Complete draft |
| POST | `/drafts/{draftId}/reset` | Reset draft |
| GET | `/drafts/{draftId}/my-turn` | Check if it's the authenticated user's turn |

### Scoring

| Method | Path | Description |
|---|---|---|
| GET/POST | `/point-rules` | Manage scoring rules per group |
| POST | `/point-calculation` | Trigger score recalculation |

### Game Data (public, read-only)

`/seasons`, `/episodes`, `/castaways`, `/challenges`, `/tribes`, `/votes`, `/tribal`, `/jury-votes`, `/journeys`, `/boots`, `/advantage-movements`

---

## Authentication

All protected endpoints require a Bearer token in the `Authorization` header:

```
Authorization: Bearer <jwt-token>
```

Public (unauthenticated) endpoints:
- `POST /api/v1/auth/**`
- `GET /api/v1/seasons/**`
- `GET /api/v1/castaways/**`
- `GET /api/v1/challenges/**`
- `GET /api/v1/episodes/**`

---

## Database Migrations

Flyway manages schema versioning. Migration files live in:

```
src/main/resources/db/migration/
└── V1__initial_schema.sql
```

Hibernate is set to `validate` — it checks schema matches entities but does not auto-create or modify tables. Always create a new migration file for schema changes.

---

## DataLoader

`DataLoader.java` runs at startup (implements `CommandLineRunner`) and loads Survivor game data from JSON files produced by the Python ETL pipeline.

**Data source paths** (configured in `DataLoader.java`):

```
/Users/joelmoffatt/VSCode/SurvivorOutDraft/survivoR/data/class-entities/   ← full load
/Users/joelmoffatt/VSCode/SurvivorOutDraft/survivoR/data/class-entities/delta/  ← incremental
```

**Load order** (respects foreign key dependencies):

1. Seasons
2. Castaways + CastawayPerformances
3. Tribes + TribeMappings
4. Episodes
5. Challenges + ChallengePerformances
6. Tribal Councils → VoteRounds → Votes
7. Jury Votes, Journeys, Boots, Advantage Movements

**Feature flags** (`application.properties`):

| Flag | Effect |
|---|---|
| `vivida.dataloader.game-data=true` | Load all Survivor game data |
| `vivida.dataloader.test-data=true` | Seed test users and groups |
| `vivida.dataloader.incremental=true` | Delta load — skip wipe, insert new records only |

See the [survivoR README](../survivoR/README.md) for how to update the JSON source files.

---

## Scoring System

Groups configure `PointRule` entries that define how many points a scoring event is worth. Scoring events include:

- `INDIVIDUAL_IMMUNITY` — won individual immunity challenge
- `FOUND_IDOL` — found a hidden immunity idol
- `SOLE_SURVIVOR` — won the game
- `TRIBAL_COUNCIL_VOTE_OUT` — voted out at tribal
- (and others per group configuration)

`PointCalculationService` scans game events and creates `TeamCastawayScoreEvent` records. Scores can be recalculated at any time via the `/point-calculation` endpoint or the `GroupScoreCalculationRun` mechanism.

---

## Running Tests

```bash
./mvnw test
```

---

## Key Design Decisions

- **Stateless API** — no server-side sessions; all auth via JWT
- **Batch inserts** — Hibernate batch size 250 for efficient DataLoader performance
- **Incremental loading** — delta mode avoids ~1-hour full reloads when only recent episodes changed
- **DTO layer** — controllers return DTOs, not raw entities, to control serialization shape
- **Feature flags** — DataLoader behavior is fully configurable without code changes
