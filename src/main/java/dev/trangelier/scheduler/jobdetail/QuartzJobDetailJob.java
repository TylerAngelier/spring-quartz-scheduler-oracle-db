package dev.trangelier.scheduler.jobdetail;

import java.util.List;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Job} implementation for the QuartzJobDetail process. This process outputs all the
 * configured jobs.
 *
 * <p>This job will read from the Quartz JDBC job detail table and output the job information.
 */
public class QuartzJobDetailJob implements Job {

  private final Logger log = LoggerFactory.getLogger(QuartzJobDetailJob.class);
  private final QuartzJobDetailDao quartzJobDetailDao;

  public QuartzJobDetailJob(QuartzJobDetailDao quartzJobDetailDao) {
    this.quartzJobDetailDao = quartzJobDetailDao;
  }

  private static long getMilliseconds(long endTime, long startTime) {
    return (endTime - startTime) / 1000000;
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("Context: {}", context);
    final var startTime = System.nanoTime();

    List<QuartzJobDetail> jobDetailList = quartzJobDetailDao.getAll();
    log.info("Jobs ({}): {}", jobDetailList.size(), jobDetailList);

    final var endTime = System.nanoTime();
    log.info("Executed Job in {}ms", getMilliseconds(endTime, startTime));
  }
}
