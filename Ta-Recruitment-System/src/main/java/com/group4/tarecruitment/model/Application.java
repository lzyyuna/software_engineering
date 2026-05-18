package com.group4.tarecruitment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents one TA application submitted for a specific job.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Application {
    private String applicationId;
    private String taId;
    private String jobId;
    private String applicationTime;
    private String status;
    private String reviewComment;

    /**
     * Creates an empty application for CSV and JSON deserialization.
     */
    public Application() {}

    /**
     * Creates a complete application record.
     *
     * @param applicationId generated application identifier
     * @param taId TA identifier of the applicant
     * @param jobId target job identifier
     * @param applicationTime submission timestamp
     * @param status review status such as Pending, Approved, or Rejected
     * @param reviewComment optional MO review comment
     */
    public Application(String applicationId, String taId, String jobId,
                       String applicationTime, String status, String reviewComment) {
        this.applicationId = applicationId;
        this.taId = taId;
        this.jobId = jobId;
        this.applicationTime = applicationTime;
        this.status = status;
        this.reviewComment = reviewComment;
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getTaId() { return taId; }
    public void setTaId(String taId) { this.taId = taId; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getApplicationTime() { return applicationTime; }
    public void setApplicationTime(String applicationTime) { this.applicationTime = applicationTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
}
