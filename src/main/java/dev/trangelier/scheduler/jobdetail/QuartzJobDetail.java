package dev.trangelier.scheduler.jobdetail;

/**
 * POJO from the qrtz_job_details table.
 *
 * @param schedName the name of the schedule.
 * @param jobName the name of the job.
 * @param jobGroup the name of the job group.
 * @param description the description of the job.
 * @param jobClassName the jobs classname.
 * @param isDurable if the job is durable.
 * @param isNonConcurrent if the job is not concurrent.
 * @param isUpdateData if the data is updatable for the job.
 * @param requestsRecovery if the job should be recoverable.
 */
public record QuartzJobDetail(
    String schedName,
    String jobName,
    String jobGroup,
    String description,
    String jobClassName,
    boolean isDurable,
    boolean isNonConcurrent,
    boolean isUpdateData,
    boolean requestsRecovery) {}
