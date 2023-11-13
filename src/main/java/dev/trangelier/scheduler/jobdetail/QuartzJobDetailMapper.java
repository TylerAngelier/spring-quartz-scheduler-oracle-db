package dev.trangelier.scheduler.jobdetail;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/** Spring JDBC {@link RowMapper} for the {@link QuartzJobDetail} class. */
public final class QuartzJobDetailMapper implements RowMapper<QuartzJobDetail> {

  @Override
  public QuartzJobDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new QuartzJobDetail(
        rs.getString("SCHED_NAME"),
        rs.getString("JOB_NAME"),
        rs.getString("JOB_GROUP"),
        rs.getString("DESCRIPTION"),
        rs.getString("JOB_CLASS_NAME"),
        rs.getBoolean("IS_DURABLE"),
        rs.getBoolean("IS_NONCONCURRENT"),
        rs.getBoolean("IS_UPDATE_DATA"),
        rs.getBoolean("REQUESTS_RECOVERY"));
  }
}
