package dev.trangelier.scheduler;

import dev.trangelier.scheduler.jobdetail.QuartzJobDetailService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The primary and foundational application class responsible for orchestrating the entire Spring Scheduler implementation,
 * leveraging the robust capabilities of the Quartz Scheduler library to manage and execute scheduled jobs within an Oracle
 * Database environment. This project represents a meticulously crafted demonstration of how to seamlessly integrate Spring Boot
 * with Quartz to create a highly scalable, reliable, and easily maintainable scheduler solution. The core objective of this project
 * is to provide a tangible and practical example of how to utilize Quartz to automate tasks, manage workflows, and ensure timely execution
 * of critical operations within a Java-based application.
 *
 * This project is fundamentally designed as a comprehensive learning tool, offering a detailed exploration of the intricacies
 * involved in setting up and configuring a Spring Scheduler with Quartz. It meticulously covers every aspect of the implementation,
 * from the initial setup of the Quartz Scheduler to the definition and scheduling of individual jobs, culminating in a fully functional
 * scheduler capable of managing tasks within an Oracle Database.
 *
 * The project's architecture is built upon a layered approach, comprising several key components, each playing a distinct and vital role
 * in the overall scheduler functionality. These components are carefully integrated to ensure a cohesive and efficient workflow,
 * maximizing performance and minimizing potential bottlenecks.
 *
 * **Key Components and Their Roles:**
 *
 * 1. **`QuartzJobDetailService`:** This class serves as the central control point for all Quartz job-related operations. It encapsulates
 *    the logic for defining, scheduling, and managing individual jobs within the Quartz Scheduler. It provides a high-level API for
 *    interacting with the scheduler, abstracting away the underlying complexities of the Quartz API.  It is responsible for creating
 *    `JobDetail` objects, scheduling them using `TriggerBuilder`, and managing their lifecycle within the Quartz Scheduler.  It handles
 *    error recovery and rescheduling of jobs in case of failures, ensuring that tasks are executed reliably.
 *
 *    - **Responsibilities:**
 *      - Creating `JobDetail` objects with appropriate job names, groups, descriptions, and schedules.
 *      - Scheduling jobs using `TriggerBuilder`, specifying start times, intervals, and other scheduling parameters.
 *      - Rescheduling jobs in case of failures or cancellations.
 *      - Monitoring the status of jobs and handling any errors that may occur.
 *
 * 2. **`QuartzJobDetail`:** This POJO represents a single Quartz job definition. It encapsulates all the relevant information
 *    associated with a job, including its name, group, description, job class name, and scheduling parameters. It provides a structured
 *    way to store and manage job definitions, facilitating easy retrieval and manipulation.
 *
 * 3. **`QuartzConfig`:** This configuration class is responsible for configuring the Quartz Scheduler, setting up the data source,
 *    job store, and thread pool. It ensures that the scheduler is properly initialized and configured for optimal performance.
 *    It also handles the connection to the Oracle Database, allowing the scheduler to interact with the database and store job definitions.
 *
 * 4. **`SchedulerApplication` (This Class):** This class is the entry point for the Spring Boot application. It initializes the Spring
 *    ApplicationContext, obtains a reference to the `QuartzJobDetailService`, and starts the scheduler. It also handles any command-line
 *    arguments passed to the application.
 *
 * **Technical Details:**
 *
 * - **Spring Boot:** This project leverages the power of Spring Boot to simplify the development process, providing features such as auto-configuration,
 *   embedded servers, and command-line support.
 * - **Quartz Scheduler:** This robust and reliable scheduler library provides a comprehensive API for managing scheduled jobs.
 * - **Oracle Database:** This project utilizes the Oracle Database as the persistent storage for job definitions.
 * - **JDBC:** The project employs JDBC to interact with the Oracle Database, allowing the scheduler to store and retrieve job definitions.
 *
 * **To Run the Project:**
 *
 * 1. Ensure that you have an Oracle Database instance running.
 * 2. Configure the `application-dev.template.yaml` file with the appropriate database connection details.
 * 3. Execute the following command: `gradle bootRun`
 *
 * This will start the scheduler and begin executing any scheduled jobs. You can view the job details by accessing the Quartz
 * Scheduler web interface at `http://localhost:8080/quartz/`.
 *
 * **Note:** This project is intended as a demonstration and learning tool. It may not be suitable for production environments
 * without further customization and testing.
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
