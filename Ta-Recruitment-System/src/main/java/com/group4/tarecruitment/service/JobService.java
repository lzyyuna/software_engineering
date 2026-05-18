package com.group4.tarecruitment.service;

import com.group4.tarecruitment.model.Application;
import com.group4.tarecruitment.model.Job;
import com.group4.tarecruitment.repository.ApplicationRepository;
import com.group4.tarecruitment.repository.JobRepository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Provides TA-facing job listing, application submission, and application history operations.
 */
public class JobService {
    private final JobRepository jobRepo;
    private final ApplicationRepository appRepo;

    /**
     * Creates a service using the default job and application data files.
     */
    public JobService() {
        this.jobRepo = new JobRepository();
        this.appRepo = new ApplicationRepository();
    }

    /**
     * Creates a service using custom job and application storage paths.
     *
     * @param jobsPath path to the jobs CSV file
     * @param appsPath path to the applications CSV file
     */
    public JobService(Path jobsPath, Path appsPath) {
        this.jobRepo = new JobRepository(jobsPath);
        this.appRepo = new ApplicationRepository(appsPath);
    }

    /**
     * Loads all recruiting jobs sorted by release time in descending order.
     *
     * @return active recruiting jobs
     * @throws Exception if job storage cannot be read
     */
    public List<Job> getActiveJobs() throws Exception {
        return jobRepo.loadAll().stream()
                .filter(j -> "Recruiting".equals(j.getStatus()))
                .sorted(Comparator.comparing(Job::getReleaseTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Submits a TA application for a job unless the applicant has already applied.
     *
     * @param taId applicant TA identifier
     * @param jobId target job identifier
     * @return generated application ID, or null when a duplicate application exists
     * @throws Exception if application storage cannot be updated
     */
    public String submitApplication(String taId, String jobId) throws Exception {
        if (hasApplied(taId, jobId)) {
            return null;
        }

        String appId = "APP-" + UUID.randomUUID().toString().substring(0, 8);
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Application app = new Application(appId, taId, jobId, time, "Pending", "");
        appRepo.save(app);
        return appId;
    }

    /**
     * Checks whether a TA has already applied for a job.
     *
     * @param taId applicant TA identifier
     * @param jobId target job identifier
     * @return true when an application already exists
     * @throws Exception if application storage cannot be read
     */
    public boolean hasApplied(String taId, String jobId) throws Exception {
        if (taId == null || jobId == null) {
            return false;
        }

        return appRepo.loadAll().stream()
                .anyMatch(a -> taId.equals(a.getTaId()) && jobId.equals(a.getJobId()));
    }

    /**
     * Loads all applications submitted by a TA sorted by application time in descending order.
     *
     * @param taId applicant TA identifier
     * @return applications submitted by the TA
     * @throws Exception if application storage cannot be read
     */
    public List<Application> getMyApplications(String taId) throws Exception {
        return appRepo.loadAll().stream()
                .filter(a -> a.getTaId().equals(taId))
                .sorted(Comparator.comparing(Application::getApplicationTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Finds a job by ID.
     *
     * @param jobId job identifier
     * @return matching job, or null when not found
     * @throws Exception if job storage cannot be read
     */
    public Job getJobById(String jobId) throws Exception {
        return jobRepo.loadAll().stream()
                .filter(j -> j.getJobId().equals(jobId))
                .findFirst().orElse(null);
    }
}
