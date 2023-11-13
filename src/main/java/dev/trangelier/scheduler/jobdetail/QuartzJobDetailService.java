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

@Service
public class QuartzJobDetailService {

  private final Logger log = LoggerFactory.getLogger(QuartzJobDetailService.class);
  private final Scheduler scheduler;

  public QuartzJobDetailService(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  public void startTasks() {
    try {
      var jobDetail = buildJobDetails();
      var trigger = buildTrigger(jobDetail);

      establishJob(jobDetail, trigger);
      log.info("{} job established.", jobDetail.getKey());
    } catch (SchedulerException e) {
      throw new IllegalStateException(e);
    }
  }

  private void establishJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
    // Only schedule if it does not exist
    var existingJob = scheduler.getJobDetail(jobDetail.getKey());

    if (existingJob == null) {
      scheduler.scheduleJob(jobDetail, trigger);
    }
    scheduler.rescheduleJob(trigger.getKey(), trigger);
  }

  private Trigger buildTrigger(JobDetail jobDetail) {
    var zonedDateTime = ZonedDateTime.now().plusSeconds(10);

    return TriggerBuilder.newTrigger()
        .forJob(jobDetail)
        .withIdentity(jobDetail.getKey().getName())
        .withDescription(jobDetail.getDescription())
        .startAt(java.util.Date.from(zonedDateTime.toInstant()))
        .withSchedule(
            SimpleScheduleBuilder.repeatSecondlyForever((int) TimeUnit.SECONDS.toSeconds(15)))
        .build();
  }

  private JobDetail buildJobDetails() {
    return JobBuilder.newJob(QuartzJobDetailJob.class)
        .withIdentity("QuartzJobDetailJob")
        .withDescription("Testing Quartz Scheduler.")
        .storeDurably()
        .requestRecovery(true)
        .build();
  }
}
