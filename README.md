# ZipRun AI Reassignment Engine — Backend

Spring Boot service implementing the reassignment domain model, a pluggable
rule-based/AI routing engine, and the agentic re-planning loop that fires
when a delivery agent goes offline. See [`../ADR.md`](../ADR.md) for the
architectural decisions behind this build.

## Tech stack

- Java 25, Spring Boot 4.1.1 (Spring Framework 7)
- Spring Data JPA + Flyway (PostgreSQL)
- Spring Validation
- [Ollama](https://ollama.com) (local LLM) — `qwen3:14b` by default
- springdoc-openapi 3.1.0 (Swagger UI)

## Prerequisites

- Java 25+
- PostgreSQL running locally, reachable on `localhost:5432`
- [Ollama](https://ollama.com) installed locally, with the `qwen3:14b` model
  pulled (or point `llm.ollama.model` at whatever you have)

## Setup (under 5 minutes)

**1. Create the database and role** (one-time):

```bash
psql postgres -c "CREATE USER hackathon WITH PASSWORD 'hackathon' SUPERUSER;"
psql postgres -c "CREATE DATABASE hackathon OWNER hackathon;"
```

**2. Start Ollama and pull the model** (if not already running):

```bash
ollama serve &
ollama pull qwen3:14b
```

**3. Run the app** — Flyway applies the schema and seed data (5 agents, 8
pre-assigned orders) automatically on startup:

```bash
./mvnw spring-boot:run
```

The API is now at `http://localhost:8080`. Swagger UI:
`http://localhost:8080/swagger-ui/index.html`.

## Configuration

All in `src/main/resources/application.yaml`:

| Property | Default | Purpose |
|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/hackathon` | Postgres connection |
| `routing.strategy` | `rule-based` | Active `RoutingStrategy` bean name at boot — flip live via `PATCH /config/routing-strategy` |
| `llm.ollama.base-url` | `http://localhost:11434` | Ollama server |
| `llm.ollama.model` | `qwen3:14b` | Model used for AI routing |
| `llm.ollama.timeout` | `30s` | Read timeout before falling back to rule-based |

## API overview

Full interactive reference in Swagger UI. Summary:

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/orders` | Create an order pre-assigned to an agent |
| `GET` | `/orders?status=` | List orders, optionally filtered |
| `POST` | `/orders/{id}/suggest` | Run the active strategy on demand |
| `GET` | `/agents` | List agents and their status |
| `PATCH` | `/agents/{id}/status` | Change agent status — `OFFLINE` fires the agentic loop, asynchronously |
| `GET` | `/suggestions?status=` | List reassignment suggestions |
| `PATCH` | `/suggestions/{id}` | Accept or reject a suggestion |
| `GET`/`PATCH` | `/config/routing-strategy` | Read or flip the active routing strategy at runtime |

## Seeing the agentic loop end-to-end

```bash
# Flip an agent with active orders offline — this call returns immediately;
# re-planning happens asynchronously.
curl -X PATCH http://localhost:8080/agents/AGT-001/status \
  -H 'Content-Type: application/json' -d '{"status":"OFFLINE"}'

# A few seconds later, suggestions appear with triggerReason = AGENT_OFFLINE
curl http://localhost:8080/suggestions?status=PENDING
```

To exercise the AI strategy specifically:

```bash
curl -X PATCH http://localhost:8080/config/routing-strategy \
  -H 'Content-Type: application/json' -d '{"strategy":"ai"}'
```

## Tests

```bash
./mvnw test
```

## Resetting to a clean seed state

```bash
psql -h localhost -U hackathon -d hackathon -c 'DROP SCHEMA public CASCADE; CREATE SCHEMA public;'
```

Flyway re-applies the schema and seed data on the next startup.
