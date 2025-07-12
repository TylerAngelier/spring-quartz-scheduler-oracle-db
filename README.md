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

Other than that, it's a basic Spring Boot application.

To run:

```bash
gradle bootRun
```

To build a docker image:

```bash
gradle bootBuildImage
```
