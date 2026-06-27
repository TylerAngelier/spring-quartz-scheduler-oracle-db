package dev.trangelier.scheduler;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import dev.trangelier.scheduler.jobdetail.QuartzJobDetail;
import dev.trangelier.scheduler.jobdetail.QuartzJobDetailDao;
import dev.trangelier.scheduler.jobdetail.QuartzJobDetailService;

import org.springframework.jdbc.core.JdbcTemplate;

import org.quartz.Scheduler;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class SchedulerApplicationTests {

  @Container
  static GenericContainer<?> oracle = new GenericContainer<>(
      DockerImageName.parse("gvenzl/oracle-free:23-slim-faststart"))
      .withExposedPorts(1521)
      .withEnv("ORACLE_RANDOM_PASSWORD", "true")
      .withEnv("APP_USER", "app_user")
      .withEnv("APP_USER_PASSWORD", "TestPassword123()")
      .withEnv("ORACLE_DATABASE", "dev")
      .waitingFor(Wait.forLogMessage(".*DATABASE IS READY.*", 1)
          .withStartupTimeout(Duration.ofMinutes(5)));

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    String host = oracle.getHost();
    Integer port = oracle.getMappedPort(1521);
    String jdbcUrl = "jdbc:oracle:thin:@" + host + ":" + port + "/dev";
    registry.add("spring.datasource.type", () -> "oracle.ucp.jdbc.PoolDataSource");
    registry.add("spring.datasource.driver-class-name", () -> "oracle.jdbc.OracleDriver");
    // Provide both 'url' (required by core DataSourceProperties.determineUrl / SB auto-config path)
    // and 'jdbcUrl' (required by this project's UCP + oracle-spring-boot-starter-ucp convention,
    // matching application*.yaml + template comments + oracleucp config).
    registry.add("spring.datasource.url", () -> jdbcUrl);
    registry.add("spring.datasource.jdbcUrl", () -> jdbcUrl);
    registry.add("spring.datasource.username", () -> "app_user");
    registry.add("spring.datasource.password", () -> "TestPassword123()");
    // replicate key oracleucp.* from application-test.yaml + quartz init
    registry.add("spring.datasource.oracleucp.connection-factory-class-name", () -> "oracle.jdbc.pool.OracleDataSource");
    registry.add("spring.datasource.oracleucp.sql-for-validate-connection", () -> "select * from dual");
    registry.add("spring.datasource.oracleucp.connection-pool-name", () -> "SchedulerApplicationTest");
    registry.add("spring.datasource.oracleucp.initial-pool-size", () -> "2");
    registry.add("spring.datasource.oracleucp.min-pool-size", () -> "2");
    registry.add("spring.datasource.oracleucp.max-pool-size", () -> "10");  // bumped for TC test (Quartz scheduler threads + store + Awaitility polling + init)
    registry.add("spring.quartz.jdbc.initialize-schema", () -> "always");  // canonical dotted (binds early for eager Quartz init pre-context; matches yamls; CI uses SPRING_ uppercase env for OS binding)
  }

  @Autowired
  QuartzJobDetailService quartzJobDetailService;

  @Autowired
  QuartzJobDetailDao quartzJobDetailDao;

  @Autowired
  JdbcTemplate jdbcTemplate;

  @Autowired
  Scheduler scheduler;

  // startTasks() called explicitly inside the execution test (per design flexibility "in @BeforeEach or the test method"; avoids side-effects in contextLoads; main hook bypass fact documented below)
  @Test
  void contextLoads() {
    // Trivial load; schema bootstrap (init=always) + TC wiring validated in execution test below.
    // See design critical fact on @SpringBootTest bypassing main hook.
  }

  @Test
  void schedulerStartsAndJobExecutesAgainstOracle() {
    // Widen the IS_* columns (Quartz >=2.5 + OracleDelegate sends full "true"/"false" strings (4 chars);
    // the standard quartz-oracle.sql + gvenzl init defines them VARCHAR2(1) causing ORA-12899 on insert into
    // job_details + fired_triggers etc.). The read-side QuartzJobDetailMapper uses getBoolean() which tolerates
    // either form. This replaces the old Quartz 2.3.2 pin shortcut while keeping modern Quartz from the SB BOM.
    jdbcTemplate.execute("ALTER TABLE qrtz_job_details MODIFY (IS_DURABLE VARCHAR2(5), IS_NONCONCURRENT VARCHAR2(5), IS_UPDATE_DATA VARCHAR2(5), REQUESTS_RECOVERY VARCHAR2(5))");
    jdbcTemplate.execute("ALTER TABLE qrtz_fired_triggers MODIFY (IS_NONCONCURRENT VARCHAR2(5), REQUESTS_RECOVERY VARCHAR2(5))");
    quartzJobDetailService.startTasks();  // explicit call here only (for execution test); addresses review + design sketch
    await().atMost(Duration.ofSeconds(60)).pollInterval(Duration.ofSeconds(2))
        .untilAsserted(() -> {
          // Schema bootstrap from init=always (lightweight check per review suggestion)
          int tables = jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM user_tables WHERE table_name LIKE 'QRTZ_%'", Integer.class);
          assertThat(tables).isGreaterThan(0);
          List<QuartzJobDetail> jobs = quartzJobDetailDao.getAll();
          assertThat(jobs).anyMatch(j -> "QuartzJobDetailJob".equals(j.jobName())
              && j.isDurable() && j.requestsRecovery());
          // Execution side-effect proof: use the persistent TIMES_TRIGGERED counter from the simple trigger
          // (fired_triggers is a transient "in-flight" table that Quartz cleans after execution, so COUNT often returns 0
          // even after successful runs — this was causing the assertion to fail despite seeing multiple "Executed Job in Xms").
          int timesTriggered = jdbcTemplate.queryForObject(
              "SELECT NVL(TIMES_TRIGGERED, 0) FROM qrtz_simple_triggers " +
              "WHERE TRIGGER_NAME = 'QuartzJobDetailJob' AND TRIGGER_GROUP = 'DEFAULT'", Integer.class);
          assertThat(timesTriggered).isGreaterThanOrEqualTo(1);
        });
  }

  // Ensure Quartz background threads (scheduler, store) are stopped before the UCP DataSource is closed
  // by the test context. Prevents "UCP pool is about to shutdown" / connection acquisition errors in the
  // QuartzSchedulerThread during/after the await (visible in logs when previous column errors caused
  // repeated retries while context lifecycle was progressing).
  @AfterEach
  void stopScheduler() throws Exception {
    if (scheduler != null && scheduler.isStarted()) {
      scheduler.shutdown(true);  // wait for running jobs
    }
  }
}
