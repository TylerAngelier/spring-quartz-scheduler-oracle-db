# AGENTS.md

Guidance for AI agents working in this repository.

## What this is

A Spring Boot proof-of-concept that schedules Quartz jobs persisted to an Oracle
database. Not a production app — keep changes minimal and behavior-preserving.

## Stack

- Spring Boot 4.1.0, Java 21, Gradle (wrapper 9.5.1)
- Quartz (`job-store-type: jdbc`, OracleDelegate)
- Oracle via Oracle UCP (`oracle-spring-boot-starter-ucp`), not Hikari
- Awaitility in tests

## Common commands

```bash
./gradlew build               # compile + test (needs Oracle running, see below)
./gradlew test --info         # run the integration test
./gradlew bootRun             # run the app (SPRING_PROFILES_ACTIVE=dev by default)
```

## Tests need a live Oracle (no Testcontainers)

The integration test (`SchedulerApplicationTests`) runs against a **real Oracle
database** — it is NOT self-contained. Oracle is supplied externally:

- **CI (Drone):** provided as a pipeline `services:` container at host `oracle`.
- **Locally:** start the docker one-liner from the README "Local Docker Oracle"
  section first, then `./gradlew test`.

The test reads the connection from env vars with these defaults (matching the
local docker setup): `ORACLE_HOST=localhost`, `ORACLE_PORT=1521`,
`ORACLE_SERVICE=dev`, `ORACLE_USER=app_user`,
`ORACLE_PASSWORD=TestPassword123()`.

Do not reintroduce Testcontainers to "fix" a failing test without checking
whether Oracle is simply not running / not ready.

## CI: Drone (not GitHub Actions)

CI runs on the homelab Drone server, defined in [`.drone.yml`](.drone.yml).

- **Test-only pipeline** — no image publish, no deploy (this is a PoC).
- Steps: start `gvenzl/oracle-free:23-slim-faststart` service (with healthcheck)
  → `./gradlew test --info` in `eclipse-temurin:21-jdk`.
- Triggers on `push` and `pull_request`.
- There is **no** `.github/workflows` anymore. If you add CI, extend `.drone.yml`.

To validate `.drone.yml` locally:

```bash
drone lint .drone.yml
drone exec .drone.yml          # runs the pipeline against the local Docker daemon
```

`drone exec` runs the real Oracle service + test step locally, so it exercises
the service DNS name (`oracle`) and sequencing identically to the server.

## Gotchas

- **Quartz `IS_*` columns:** modern Quartz (2.5+, what the SB 4.1 BOM pulls) writes
  full `"true"`/`"false"` strings via `OracleDelegate`, which overflow the legacy
  `VARCHAR2(1)` schema columns (`ORA-12899`). The test widens them at runtime via
  `ALTER TABLE ... MODIFY (... VARCHAR2(5))`; `src/main/resources/quartz-oracle.sql`
  is also widened for the standalone-init path.
- **`url` vs `jdbcUrl`:** Spring Boot 4's `DataSourceProperties.determineUrl()`
  reads `spring.datasource.url`, but the UCP starter convention uses `jdbcUrl`.
  The test sets **both**. Omitting `url` causes `Failed to determine suitable jdbc url`.
- **Scheduler shutdown:** the test shuts the Quartz `Scheduler` down in `@AfterEach`
  before UCP closes, to avoid pool-shutdown errors in Quartz worker threads.

## Conventions

- Persist PoC behavior; don't gold-plate.
- Keep config in `application*.yaml` / `application-dev.template.yaml`; secrets
  via env / Drone secrets, never committed.
