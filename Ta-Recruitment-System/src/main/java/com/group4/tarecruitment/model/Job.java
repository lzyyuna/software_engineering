package com.group4.tarecruitment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a TA recruitment position posted by an MO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {
    private String jobId;
    private String courseName;
    private String positionType;
    private int weeklyWorkload;
    private String moName;
    private String moEmail;
    private String status;
    private String releaseTime;
    private String skillRequirements;
    private String jobContent;
    private String deadline;
    private String department;

    /**
     * Creates an empty job for CSV and JSON deserialization.
     */
    public Job() {}

    /**
     * Creates a complete job record.
     *
     * @param jobId generated job identifier
     * @param courseName course name
     * @param positionType type of TA position
     * @param weeklyWorkload expected weekly workload in hours
     * @param moName responsible MO name
     * @param moEmail responsible MO email
     * @param status job status such as Recruiting or Closed
     * @param releaseTime job release timestamp
     * @param skillRequirements required skills for matching
     * @param jobContent description of TA responsibilities
     * @param deadline application deadline
     * @param department department that owns the position
     */
    public Job(String jobId, String courseName, String positionType, int weeklyWorkload,
               String moName, String moEmail, String status, String releaseTime, String skillRequirements,
               String jobContent, String deadline, String department) {
        this.jobId = jobId;
        this.courseName = courseName;
        this.positionType = positionType;
        this.weeklyWorkload = weeklyWorkload;
        this.moName = moName;
        this.moEmail = moEmail;
        this.status = status;
        this.releaseTime = releaseTime;
        this.skillRequirements = skillRequirements;
        this.jobContent = jobContent;
        this.deadline = deadline;
        this.department = department;
    }

    /**
     * Creates a job record with the default department value.
     *
     * @param jobId generated job identifier
     * @param courseName course name
     * @param positionType type of TA position
     * @param weeklyWorkload expected weekly workload in hours
     * @param moName responsible MO name
     * @param moEmail responsible MO email
     * @param status job status such as Recruiting or Closed
     * @param releaseTime job release timestamp
     * @param skillRequirements required skills for matching
     * @param jobContent description of TA responsibilities
     * @param deadline application deadline
     */
    public Job(String jobId, String courseName, String positionType, int weeklyWorkload,
               String moName, String moEmail, String status, String releaseTime, String skillRequirements,
               String jobContent, String deadline) {
        this(jobId, courseName, positionType, weeklyWorkload, moName, moEmail, status,
                releaseTime, skillRequirements, jobContent, deadline, "General/Others");
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getPositionType() { return positionType; }
    public void setPositionType(String positionType) { this.positionType = positionType; }
    public int getWeeklyWorkload() { return weeklyWorkload; }
    public void setWeeklyWorkload(int weeklyWorkload) { this.weeklyWorkload = weeklyWorkload; }
    public String getMoName() { return moName; }
    public void setMoName(String moName) { this.moName = moName; }
    public String getMoEmail() { return moEmail; }
    public void setMoEmail(String moEmail) { this.moEmail = moEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReleaseTime() { return releaseTime; }
    public void setReleaseTime(String releaseTime) { this.releaseTime = releaseTime; }
    public String getSkillRequirements() { return skillRequirements; }
    public void setSkillRequirements(String skillRequirements) { this.skillRequirements = skillRequirements; }
    public String getJobContent() { return jobContent; }
    public void setJobContent(String jobContent) { this.jobContent = jobContent; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String toString() {
        return courseName + " (" + positionType + ") - " + jobId;
    }
}
