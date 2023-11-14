# Scheduler

I mean... Everyone wants to build a scheduler, right?

![Build Passing](https://github.com/tylerangelier/spring-quartz-scheduler-oracle-db/actions/workflows/gradle.yml/badge.svg)

# Usage

Probably none. Application used to prove out Spring Scheduler using Quartz with a persistence tier
using an Oracle Database.

# Development

You'll need an Oracle Database. I'm using an
[Oracle Database Free docker image](https://github.com/gvenzl/oci-oracle-free) on a VM in my
home-lab. You probably also need to toggle `spring.quartz.jdbc.initialize-schema` to `always` to
set up the schema. You instead could run
the [schema](https://github.com/quartznet/quartznet/blob/main/database/tables/tables_oracle.sql)
file separately.

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