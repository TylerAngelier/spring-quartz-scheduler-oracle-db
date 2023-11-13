package dev.trangelier.scheduler.jobdetail;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Repository for the {@link QuartzJobDetail} POJO. */
@Repository
public class QuartzJobDetailDao {
  private static final String SQL_GET_ALL = "SELECT * FROM qrtz_job_details";
  private final JdbcTemplate jdbcTemplate;

  public QuartzJobDetailDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * List of all jobs currently configured.
   *
   * @return collection of all jobs.
   */
  public List<QuartzJobDetail> getAll() {
    return jdbcTemplate.query(SQL_GET_ALL, new QuartzJobDetailMapper());
  }
}
