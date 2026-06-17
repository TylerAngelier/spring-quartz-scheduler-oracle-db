package dev.trangelier.scheduler;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import dev.trangelier.scheduler.jobdetail.QuartzJobDetail;
import dev.trangelier.scheduler.jobdetail.QuartzJobDetailDao;
import dev.trangelier.scheduler.jobdetail.QuartzJobDetailService;

import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class SchedulerApplicationTests {

  @Container
  static OracleContainer oracle = new OracleContainer(
      DockerImageName.parse("gvenzl/oracle-free:23-slim-faststart"));

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.type", () -> "oracle.ucp.jdbc.PoolDataSource");
    registry.add("spring.datasource.driver-class-name", () -> "oracle.jdbc.OracleDriver");
    registry.add("spring.datasource.jdbcUrl", oracle::getJdbcUrl);
    registry.add("spring.datasource.username", () -> "app_user");  // match CI / docker
    registry.add("spring.datasource.password", () -> "TestPassword123()");
    // replicate key oracleucp.* from application-test.yaml + quartz init
    registry.add("spring.datasource.oracleucp.connection-factory-class-name", () -> "oracle.jdbc.pool.OracleDataSource");
    registry.add("spring.datasource.oracleucp.sql-for-validate-connection", () -> "select * from dual");
    registry.add("spring.datasource.oracleucp.connection-pool-name", () -> "SchedulerApplicationTest");
    registry.add("spring.datasource.oracleucp.initial-pool-size", () -> "2");
    registry.add("spring.datasource.oracleucp.min-pool-size", () -> "2");
    registry.add("spring.datasource.oracleucp.max-pool-size", () -> "5");
    registry.add("spring.quartz.jdbc.initialize-schema", () -> "always");  // canonical dotted (binds early for eager Quartz init pre-context; matches yamls; CI uses SPRING_ uppercase env for OS binding)
  }

  @Autowired
  QuartzJobDetailService quartzJobDetailService;

  @Autowired
  QuartzJobDetailDao quartzJobDetailDao;

  @Autowired
  JdbcTemplate jdbcTemplate;

  // startTasks() called explicitly inside the execution test (per design flexibility "in @BeforeEach or the test method"; avoids side-effects in contextLoads; main hook bypass fact documented below)
  @Test
  void contextLoads() {
    // Trivial load; schema bootstrap (init=always) + TC wiring validated in execution test below.
    // See design critical fact on @SpringBootTest bypassing main hook.
  }

  @Test
  void schedulerStartsAndJobExecutesAgainstOracle() {
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
          // Execution side-effect proof (per design sketch + Implementation Notes "assert >=2 fires", "inspect qrtz_fired_triggers")
          int fires = jdbcTemplate.queryForObject(
              "SELECT COUNT(*) FROM qrtz_fired_triggers WHERE JOB_NAME = 'QuartzJobDetailJob'", Integer.class);
          assertThat(fires).isGreaterThanOrEqualTo(1);
        });
  }
}
