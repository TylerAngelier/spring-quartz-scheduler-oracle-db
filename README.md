# Scheduler

I mean... Everyone wants to build a scheduler, right?

![Build Passing](https://github.com/tylerangelier/spring-quartz-scheduler-oracle-db/actions/workflows/gradle.yml/badge.svg)

# Usage

Probably none. Application used to prove out Spring Scheduler using Quartz with a persistence tier
using an Oracle Database.

# Development

You'll need an Oracle Database.

## Local Oracle Database Setup

You can run a local Oracle Database for development using the official Docker image.

1.  **Pull the image:**

You can find the official image on the [Oracle Container Registry](https://container-registry.oracle.com/ords/f?p=113:4:100454710588465:::4:P4_REPOSITORY,AI_REPOSITORY,AI_REPOSITORY_NAME,P4_REPOSITORY_NAME,P4_EULA_ID,P4_BUSINESS_AREA_ID:1863,1863,Oracle%20Database%20Free,Oracle%20Database%20Free,1,0&cs=3z_zXWg5m6rPnhmGGCsTZtjR1JMwpxxVpi_O74vYKzSVt-Rl5RwS6jcZ0bhtfdiXO52KNT3Z_cPb8Zu9bFsLctA).

2.  **Run the container:**

The following command will start an Oracle Database Free container.

```bash
docker volume create oracle-db
docker run -d --name oracle-db-free \
-p 1521:1521 \
-e ORACLE_PWD=Super_S3cur3 \
-e ENABLE_ARCHIVELOG=true \
-e ENABLE_FORCE_LOGGING=true \
-v oracle-db:/opt/oracle/oradata \
container-registry.oracle.com/database/free:latest
```

- `-d`: Runs the container in detached mode.
- `--name oracle-db-free`: Assigns a name to the container.
- `-p 1521:1521`: Maps the container's port 1521 to the host's port 1521.
- `-e ORACLE_PWD=Super_S3cur3`: Sets the password for the `SYS`, `SYSTEM`, and `PDBADMIN` accounts. **Change this to a secure password.**
- `-e ENABLE_ARCHIVELOG=true`: Enables archivelog mode, which is good practice for databases.
- `-e ENABLE_FORCE_LOGGING=true`: Forces logging of all transactions.
- `-v oracle-db:/opt/oracle/oradata`: Uses a Docker volume named `oracle-db` to persist the database data.

After setting up the database, you'll want an application user to run the app with. A script has been provided in the `src/main/resources` directory.

```bash
sql sys/Super_S3cur3@localhost:1521/FREEPDB1 @src/main/resources/database.sql
```

Next you will need to also need to toggle `spring.quartz.jdbc.initialize-schema` to `always` to
set up the Quartz schema. You instead could run
the [schema](https://github.com/quartznet/quartznet/blob/main/database/tables/tables_oracle.sql)
file separately.

```bash
sql sys/Super_S3cur3@localhost:1521/FREEPDB1 @src/main/resources/quartz-oracle.sql
```

There is a `dev` profile that is activated by default. There is a
sample file, `application-dev.template.yaml`, that has the values you most likely need to customize.

Other than that, it's a basic Spring Boot application. Requires Java 21+ (sourceCompatibility '21') and Gradle (wrapper 9.5.1).

## Local Docker Oracle (exact one-liner per design Implementation Notes + Baseline)

```bash
docker run -d --name oracle -p 1521:1521 -e ORACLE_RANDOM_PASSWORD=true -e APP_USER=app_user -e APP_USER_PASSWORD="TestPassword123()" -e ORACLE_DATABASE=dev gvenzl/oracle-free:23-slim-faststart
# Wait for ready:
while ! docker logs oracle 2>/dev/null | grep -qi 'DATABASE IS READY'; do sleep 5; done
# or: while ! docker exec oracle healthcheck.sh; do sleep 5; done
```

Copy `application-dev.template.yaml` → `application-dev.yml` (gitignored), edit creds/URL if needed. (Use `jdbcUrl` key for UCP + explicit type + oracleucp.* props + SPRING_DATASOURCE_JDBCURL overrides; see template comment + main/test yamls + TC @Dynamic.)

## Run

```bash
./gradlew bootRun
```

Expected (after ~10s schedule + every 15s): repeated

```
... QuartzJobDetailService ... job established.
...
... QuartzJobDetailJob : Jobs (1): [QuartzJobDetail[schedName=..., jobName=QuartzJobDetailJob, ..., isDurable=true, ..., requestsRecovery=true]]
... QuartzJobDetailJob : Executed Job in Xms
```

## Test (self-contained TC + Awaitility, no external DB)

```bash
./gradlew test --info
```

Uses @Testcontainers gvenzl/oracle-free:23-slim-faststart + full @DynamicPropertySource (all DS/UCP/quartz) + explicit startTasks() + Awaitility on DAO for job row + durable/recovery.

## Build

```bash
./gradlew build
```

## Post-change verification checklist (per design Baseline box + Implementation Notes)

- `./gradlew dependencies --configuration runtimeClasspath | diff -u /tmp/baseline-runtime.txt - || true` (modern: SB4.1 + jdbc + direct ucp 26.2.0, no data-jdbc bloat/old catalog/ojdbc11/Hikari)
- `./gradlew test --info` (green; TC + Awaitility assert passes with job row + >=1 fire in qrtz_job_details)
- (docker oracle running) `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun 2>&1 | tee ... | grep -E 'Jobs \([0-9]+\):|Executed Job in' | tail -5` (repeated "Jobs (1): [QuartzJobDetail[...]]" + "Executed Job in Xms" >=2x, no errors)
- `grep -E 'Deprecated Gradle features were used|incompatible with Gradle 9.0' ... || echo 'Gradle 9 clean (post PR3)'`
- `./gradlew build -x test --warning-mode all 2>&1 | tail -5`
- Optional row verification (per notes): `docker exec -it oracle sqlplus -s app_user/"TestPassword123()"@//localhost:1521/dev <<'EOF' SELECT ... FROM qrtz_job_details WHERE job_name = 'QuartzJobDetailJob'; SELECT COUNT(*) FROM qrtz_fired_triggers ... EOF`

See full design for PR plan, Implementation Notes (test wiring, QuartzConfig decision, migrator, etc.): /private/tmp/grok-design-doc-7badfbd8.md (and /tmp equiv).

Tests use Testcontainers (Oracle gvenzl image) + Awaitility for scheduler/job execution + persistence validation against real Oracle (see SchedulerApplicationTests). No external DB required for `./gradlew test`.
