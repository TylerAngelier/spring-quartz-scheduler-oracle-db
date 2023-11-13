package dev.trangelier.scheduler.jobdetail;

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
