package dev.trangelier.scheduler;

import dev.trangelier.scheduler.jobdetail.QuartzJobDetailService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main application class that starts the scheduler.
 *
 * This project is a demonstration of using Spring Scheduler with Quartz to manage jobs in an Oracle
 * Database. It includes a service for defining and scheduling Quartz jobs, as well as a DAO for
 * retrieving job details from the database. This project showcases how to use Quartz with Spring
 * Boot to create a robust and scalable scheduler that can be easily integrated into any Java-based
 * application.
 *
 * The project consists of several key components:
 * - {@link QuartzJobDetailService}: A service class responsible for defining and scheduling Quartz
 *   jobs. It uses the Quartz Scheduler API to manage job details, including job names, groups,
 *   descriptions, and schedules.
 * - {@link QuartzJobDetailDao}: A data access object (DAO) that provides a layer of abstraction
 *   between the business logic and the database. It uses Spring JDBC to interact with the Oracle
 *   Database and retrieve job details.
 *
 * The project also includes several configuration classes:
 * - {@link QuartzConfig}: Configures the Quartz Scheduler, including setting up the data source,
 *   job store, and thread pool.
 * - {@link application-dev.template.yaml}: A sample configuration file that demonstrates how to
 *   configure the Spring Boot application for development purposes.
 *
 * To run the project, simply execute the following command:
 * ```bash
 * gradle bootRun
 * ```
 *
 * This will start the scheduler and begin executing any scheduled jobs. You can view the job
 * details by accessing the Quartz Scheduler web interface at `http://localhost:8080/quartz/`.
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
```