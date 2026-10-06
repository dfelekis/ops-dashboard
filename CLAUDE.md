# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Marlowe & Finch operations dashboard: a read-only Spring Boot REST API (`/api/*`) over PostgreSQL, plus a no-build vanilla-JS page served from `src/main/resources/static/`. The repo doubles as material for a Claude Code workshop (labs in `workshop/`, tickets in `docs/tickets/TODO-231..233.md`).

## Commands

```bash
docker compose up -d db                          # Postgres 16 on :5432 (db/user/password "ops")
./mvnw spring-boot:run                           # http://localhost:8080 (Flyway migrates + seeds on first start)
SPRING_PROFILES_ACTIVE=demo ./mvnw spring-boot:run   # in-memory H2, no Docker needed (PowerShell: $env:SPRING_PROFILES_ACTIVE='demo')

./mvnw test                                      # Java tests (H2 + MockMvc), 25 currently
./mvnw test -Dtest=DashboardControllerTest#methodName   # single Java test
npm install && npm test                          # Jest + jsdom frontend tests, 45 currently
npx jest src/test/javascript/render.test.js -t "name"   # single Jest file / test
```

There is no lint step. `tools/make_seed.py` regenerates `V2__seed.sql` and `docs/data/deliveries-last-30-days.csv`.

## Rules

- **`pom.xml` dependencies are frozen** (changes need a CHG ticket). Do not add dependencies, including validation libraries.
- Plain SQL via `NamedParameterJdbcTemplate`; no JPA/ORM.

## Architecture

- **Backend** (`com.marlowefinch.ops`): controllers are thin; all SQL lives in `DashboardRepository` (kpis, on-time by carrier, late deliveries, tickets by category) and `VendorRepository`. Responses are Java records.
- **Date handling:** every endpoint takes optional `from`/`to`, resolved by `DateRange.resolve(from, to, clock)` (default: last 30 days ending "today"). Invalid input (non-ISO date, `from > to`, span over 366 days, `limit` outside 1..500) throws `ValidationException`, rendered by `ApiExceptionHandler` as 400 `{"errors":[...]}` (TODO-232).
- **Pinned clock:** `ClockConfig` fixes "today" to `ops.today=2026-09-21`, regardless of the real date. Use the injected `Clock`, never `LocalDate.now()`.
- **Profiles:** default `postgres` (env `DATABASE_URL`/`DATABASE_USER`/`DATABASE_PASSWORD`); `demo` and the tests use H2 in PostgreSQL mode. Keep SQL compatible with both.
- **Query conventions:** delivery metrics count by `delivered_date` (in-transit excluded); on time = `delivered_date <= promised_date`; orders count by `ordered_at`, revenue excludes cancelled; tickets count by `opened_at`.
- **Frontend:** `app.js` is wrapped in `initApp(document, fetchImpl)` so the same code runs in the browser and under Jest. Charts are inline SVG. The Jest harness (`src/test/javascript/harness`) registers known DOM element ids — any new id added to `index.html` must also be registered there.
- **Migrations:** Flyway in `src/main/resources/db/migration` (`V1__schema.sql`, generated `V2__seed.sql`); don't hand-edit the seed, regenerate it.

## Workshop notes

`.mcp.json` configures a read-only `postgres` MCP server (needs the Docker DB running). `docs/examples/` holds reference agents, hooks, skills and a CI workflow that are intentionally not active.
