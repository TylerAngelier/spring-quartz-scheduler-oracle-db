package dev.trangelier.scheduler;

import dev.trangelier.scheduler.jobdetail.QuartzJobDetailService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main application class that starts the scheduler.
 *
 * @author Tyler Angelier
 */
@SpringBootApplication
public class SchedulerApplication {

  public static void main(String[] args) {
    var applicationContext = SpringApplication.run(SchedulerApplication.class, args);

    // Start tasks
    var quartzJobDetailService = applicationContext.getBean(QuartzJobDetailService.class);
    quartzJobDetailService.startTasks();
  }
}
