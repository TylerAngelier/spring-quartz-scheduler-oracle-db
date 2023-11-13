package dev.trangelier.scheduler.jobdetail;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class QuartzJobDetailDao {
  private static final String SQL_GET_ALL = "SELECT * FROM qrtz_job_details";
  private final JdbcTemplate jdbcTemplate;

  public QuartzJobDetailDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<QuartzJobDetail> getAll() {
    return jdbcTemplate.query(SQL_GET_ALL, new QuartzJobDetailMapper());
  }
}
