package dev.trangelier.scheduler.jobdetail;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for {@link QuartzJobDetail}. Handles defining and all scheduling operations for the
 * {@link QuartzJobDetailJob}.
 *
 * <p>TODO: Refactor these methods to an interface and allow the delegate pattern to create all
 * jobs.
 */
@Service
public class QuartzJobDetailService {

  private final Logger log = LoggerFactory.getLogger(QuartzJobDetailService.class);
  private final Scheduler scheduler;

  public QuartzJobDetailService(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  /** Starts all tasks managed by this class. */
  public void startTasks() {
    try {
      var jobDetail = buildJobDetails();
      log.info("Building Job Details: {}", jobDetail);
      var trigger = buildTrigger(jobDetail);
      log.info("Building Trigger: {}", trigger);

      establishJob(jobDetail, trigger);
      log.info("{} job established.", jobDetail.getKey());
    } catch (SchedulerException e) {
      log.error("Error establishing Quartz Scheduler", e);
      throw new IllegalStateException(e);
    }
  }

  private void establishJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
    // Only schedule if it does not exist
    var existingJob = scheduler.getJobDetail(jobDetail.getKey());

    if (existingJob == null) {
      try {
        scheduler.scheduleJob(jobDetail, trigger);
        log.info("Scheduled Job: {}", jobDetail.getKey());
      } catch (SchedulerException e) {
        log.error("Error scheduling Quartz Job", e);
        throw new SchedulerException("Failed to schedule Quartz Job");
      }
    } else {
      log.info("Quartz Job already exists. Rescheduling...");
      try {
        scheduler.rescheduleJob(trigger.getKey(), trigger);
        log.info("Rescheduled Quartz Job: {}", jobDetail.getKey());
      } catch (SchedulerException e) {
        log.error("Error rescheduling Quartz Job", e);
        throw new SchedulerException("Failed to reschedule Quartz Job");
      }
    }
  }

  private Trigger buildTrigger(JobDetail jobDetail) {
    var zonedDateTime = ZonedDateTime.now().plusSeconds(10);

    try {
      return TriggerBuilder.newTrigger()
          .forJob(jobDetail)
          .withIdentity(jobDetail.getKey().getName())
          .withDescription(jobDetail.getDescription())
          .startAt(java.util.Date.from(zonedDateTime.toInstant()))
          .withSchedule(
              SimpleScheduleBuilder.repeatSecondlyForever((int) TimeUnit.SECONDS.toSeconds(15)))
          .build();
    } catch (Exception e) {
      log.error("Error building Quartz Trigger", e);
      throw new SchedulerException("Failed to build Quartz Trigger");
    }
  }

  private JobDetail buildJobDetails() {
    try {
      return JobBuilder.newJob(QuartzJobDetailJob.class)
          .withIdentity("QuartzJobDetailJob")
          .withDescription("Testing Quartz Scheduler.")
          .storeDurably()
          .requestRecovery(true)
          .build();
    } catch (Exception e) {
      log.error("Error building Quartz Job Details", e);
      throw new SchedulerException("Failed to build Quartz Job Details");
    }
  }
}
