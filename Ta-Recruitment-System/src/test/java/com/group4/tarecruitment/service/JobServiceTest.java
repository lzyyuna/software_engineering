package com.group4.tarecruitment.service;

import com.group4.tarecruitment.model.Application;
import com.group4.tarecruitment.model.Job;
import com.group4.tarecruitment.repository.ApplicationRepository;
import com.group4.tarecruitment.repository.JobRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JobService.
 *
 * 为什么用集成测试而不是 Mock：
 *   JobService 的排序、防重复、过滤逻辑都依赖从文件读取的真实数据。
 *   Mock 掉 Repository 后无法验证"写入再读出"的完整数据流。
 *
 * 优化点：
 *   1. 用 @TempDir 替代手动删除文件
 *   2. 用 @BeforeEach 统一初始化 service 和 repository
 *   3. 提取 Job / Application 工厂方法，减少重复
 *   4. 每个测试只断言一件事，报错信息更精准
 */
@DisplayName("JobService Integration Tests")
class JobServiceTest {

    @TempDir
    Path tempDir;

    private JobService jobService;
    private JobRepository jobRepo;
    private ApplicationRepository appRepo;

    @BeforeEach
    void setUp() {
        Path jobsPath = tempDir.resolve("jobs.csv");
        Path appsPath = tempDir.resolve("applications.csv");

        jobRepo    = new JobRepository(jobsPath);
        appRepo    = new ApplicationRepository(appsPath);
        jobService = new JobService(jobsPath, appsPath);
    }

    // ── 工厂方法 ─────────────────────────────────────────────────────────────

    private Job job(String id, String course, String mo, String status, String releaseTime) {
        return new Job(id, course, "Module TA", 10, mo, mo + "@bupt.edu",
                status, releaseTime, "skill", "help", "2026-9");
    }

    private Application app(String appId, String taId, String jobId,
                            String time, String status) {
        return new Application(appId, taId, jobId, time, status, "");
    }

    // =========================================================================
    // getActiveJobs
    // =========================================================================

    @Test
    @DisplayName("getActiveJobs: excludes Closed jobs")
    void getActiveJobs_excludesClosedJobs() throws Exception {
        jobRepo.saveAll(List.of(
                job("JOB-1", "math", "mo", "Recruiting", "2026-03-01 10:00:00"),
                job("JOB-2", "java", "mo", "Closed",     "2026-03-05 10:00:00")
        ));

        List<Job> active = jobService.getActiveJobs();

        assertEquals(1, active.size());
        assertEquals("JOB-1", active.get(0).getJobId());
    }

    @Test
    @DisplayName("getActiveJobs: all results have Recruiting status")
    void getActiveJobs_allResultsAreRecruiting() throws Exception {
        jobRepo.saveAll(List.of(
                job("JOB-1", "math", "mo", "Recruiting", "2026-03-01 10:00:00"),
                job("JOB-2", "java", "mo", "Closed",     "2026-03-05 10:00:00"),
                job("JOB-3", "math", "mo", "Recruiting", "2026-03-10 23:31:18")
        ));

        List<Job> active = jobService.getActiveJobs();

        active.forEach(j -> assertEquals("Recruiting", j.getStatus()));
    }

    @Test
    @DisplayName("getActiveJobs: sorted by releaseTime descending")
    void getActiveJobs_sortedByReleaseTimeDesc() throws Exception {
        jobRepo.saveAll(List.of(
                job("JOB-1", "math", "mo", "Recruiting", "2026-03-01 10:00:00"),
                job("JOB-3", "math", "mo", "Recruiting", "2026-03-10 23:31:18")
        ));

        List<Job> active = jobService.getActiveJobs();

        assertEquals("JOB-3", active.get(0).getJobId(), "Newer job should be first");
        assertEquals("JOB-1", active.get(1).getJobId(), "Older job should be second");
    }

    // =========================================================================
    // submitApplication
    // =========================================================================

    @Test
    @DisplayName("submitApplication: first submission returns APP- prefixed ID")
    void submitApplication_firstTime_returnsAppId() throws Exception {
        String appId = jobService.submitApplication("TA-001", "JOB-XYZ");

        assertNotNull(appId);
        assertTrue(appId.startsWith("APP-"), "Application ID should start with APP-");
    }

    @Test
    @DisplayName("submitApplication: saved with Pending status and correct fields")
    void submitApplication_firstTime_savedWithCorrectFields() throws Exception {
        String appId = jobService.submitApplication("TA-001", "JOB-XYZ");

        List<Application> all = appRepo.loadAll();
        assertEquals(1, all.size());

        Application saved = all.get(0);
        assertEquals(appId,     saved.getApplicationId());
        assertEquals("TA-001",  saved.getTaId());
        assertEquals("JOB-XYZ", saved.getJobId());
        assertEquals("Pending", saved.getStatus());
        assertEquals("",        saved.getReviewComment());
    }

    @Test
    @DisplayName("submitApplication: second submission to same job returns null")
    void submitApplication_secondTime_returnsNull() throws Exception {
        jobService.submitApplication("TA-002", "JOB-ABC");
        String second = jobService.submitApplication("TA-002", "JOB-ABC");

        assertNull(second, "Duplicate submission should return null");
    }

    @Test
    @DisplayName("submitApplication: second submission does not create duplicate record")
    void submitApplication_secondTime_noDuplicateRecord() throws Exception {
        jobService.submitApplication("TA-002", "JOB-ABC");
        jobService.submitApplication("TA-002", "JOB-ABC");

        assertEquals(1, appRepo.loadAll().size(), "Only one record should exist after duplicate submission");
    }

    // =========================================================================
    // getMyApplications
    // =========================================================================

    @Test
    @DisplayName("getMyApplications: only returns applications belonging to the TA")
    void getMyApplications_filtersByTaId() throws Exception {
        appRepo.saveAll(List.of(
                app("APP-1", "TA-1", "JOB-1", "2026-04-01 10:00:00", "Pending"),
                app("APP-2", "TA-1", "JOB-2", "2026-04-02 09:00:00", "Approved"),
                app("APP-3", "TA-2", "JOB-1", "2026-04-03 08:00:00", "Pending")
        ));

        List<Application> mine = jobService.getMyApplications("TA-1");

        assertEquals(2, mine.size());
        mine.forEach(a -> assertEquals("TA-1", a.getTaId()));
    }

    @Test
    @DisplayName("getMyApplications: sorted by applicationTime descending")
    void getMyApplications_sortedByTimeDesc() throws Exception {
        appRepo.saveAll(List.of(
                app("APP-1", "TA-1", "JOB-1", "2026-04-01 10:00:00", "Pending"),
                app("APP-2", "TA-1", "JOB-2", "2026-04-02 09:00:00", "Approved")
        ));

        List<Application> mine = jobService.getMyApplications("TA-1");

        assertEquals("APP-2", mine.get(0).getApplicationId(), "Newer application should come first");
        assertEquals("APP-1", mine.get(1).getApplicationId());
    }

    // =========================================================================
    // getJobById
    // =========================================================================

    @Test
    @DisplayName("getJobById: returns correct job when ID exists")
    void getJobById_existingId_returnsJob() throws Exception {
        jobRepo.saveAll(List.of(
                job("JOB-A", "math", "mo", "Recruiting", "2026-03-01 10:00:00"),
                job("JOB-B", "java", "mo", "Recruiting", "2026-03-02 10:00:00")
        ));

        Job found = jobService.getJobById("JOB-B");

        assertNotNull(found);
        assertEquals("java", found.getCourseName());
    }

    @Test
    @DisplayName("getJobById: returns null when ID does not exist")
    void getJobById_missingId_returnsNull() throws Exception {
        jobRepo.saveAll(List.of(
                job("JOB-A", "math", "mo", "Recruiting", "2026-03-01 10:00:00")
        ));

        assertNull(jobService.getJobById("JOB-MISSING"));
    }
}