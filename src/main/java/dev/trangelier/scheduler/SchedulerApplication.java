package dev.trangelier.scheduler;

import dev.trangelier.scheduler.jobdetail.QuartzJobDetailService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SchedulerApplication {

  public static void main(String[] args) {
    var applicationContext = SpringApplication.run(SchedulerApplication.class, args);

    // Start tasks
    var quartzJobDetailService = applicationContext.getBean(QuartzJobDetailService.class);
    quartzJobDetailService.startTasks();
  }
}
