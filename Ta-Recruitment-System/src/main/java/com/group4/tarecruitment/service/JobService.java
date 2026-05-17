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

public class JobService {
    private final JobRepository jobRepo;
    private final ApplicationRepository appRepo;

    public JobService() {
        this.jobRepo = new JobRepository();
        this.appRepo = new ApplicationRepository();
    }

    public JobService(Path jobsPath, Path appsPath) {
        this.jobRepo = new JobRepository(jobsPath);
        this.appRepo = new ApplicationRepository(appsPath);
    }

    // TA-003: 获取所有 Recruiting 状态的岗位，按发布时间倒序
    public List<Job> getActiveJobs() throws Exception {
        return jobRepo.loadAll().stream()
                .filter(j -> "Recruiting".equals(j.getStatus()))
                .sorted(Comparator.comparing(Job::getReleaseTime).reversed())
                .collect(Collectors.toList());
    }

    // TA-004: 提交申请
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

    public boolean hasApplied(String taId, String jobId) throws Exception {
        if (taId == null || jobId == null) {
            return false;
        }

        return appRepo.loadAll().stream()
                .anyMatch(a -> taId.equals(a.getTaId()) && jobId.equals(a.getJobId()));
    }

    // TA-005: 获取当前 TA 的所有申请记录，按申请时间倒序
    public List<Application> getMyApplications(String taId) throws Exception {
        return appRepo.loadAll().stream()
                .filter(a -> a.getTaId().equals(taId))
                .sorted(Comparator.comparing(Application::getApplicationTime).reversed())
                .collect(Collectors.toList());
    }

    // 根据 jobId 获取岗位详情
    public Job getJobById(String jobId) throws Exception {
        return jobRepo.loadAll().stream()
                .filter(j -> j.getJobId().equals(jobId))
                .findFirst().orElse(null);
    }
}
