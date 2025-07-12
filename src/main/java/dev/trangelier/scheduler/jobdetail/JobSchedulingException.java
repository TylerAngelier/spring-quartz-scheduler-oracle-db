package dev.trangelier.scheduler.jobdetail;

/**
 * Custom exception for job scheduling failures.
 */
public class JobSchedulingException extends RuntimeException {

  public JobSchedulingException(String message) {
    super(message);
  }

  public JobSchedulingException(String message, Exception cause) {
    super(message, cause);
  }
}
