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
 * Integration tests for MOService.
 *
 * 为什么用集成测试而不是 Mock：
 *   reviewApplication() 的权限校验依赖"读 Job 文件确认 moName"，
 *   状态变更依赖"读出 Application → 修改 → 写回文件"的完整流程。
 *   Mock 掉后只能验证调用链，无法验证数据是否真的被正确持久化。
 *
 * 优化点：
 *   1. 用 @TempDir 替代手动删文件，每个测试完全隔离
 *   2. 用 @BeforeEach 统一初始化依赖
 *   3. 提取 seedJob / seedApp / findById 辅助方法，消除重复
 *   4. 每个测试的 Arrange 只准备当前测试需要的最少数据
 */
@DisplayName("MOService Integration Tests")
public class MOServiceTest {

    @TempDir
    Path tempDir;

    private MOService moService;
    private JobRepository jobRepo;
    private ApplicationRepository appRepo;

    @BeforeEach
    void setUp() {
        Path jobsPath = tempDir.resolve("jobs.csv");
        Path appsPath = tempDir.resolve("applications.csv");

        jobRepo   = new JobRepository(jobsPath);
        appRepo   = new ApplicationRepository(appsPath);
        moService = new MOService(jobsPath, appsPath);
    }

    // ── 辅助方法 ─────────────────────────────────────────────────────────────

    /** 写入单个 Job 到文件 */
    private void seedJob(String jobId, String course, String moName) throws Exception {
        seedJob(jobId, course, moName, "Recruiting", "2026-03-01 10:00:00");
    }

    /** 写入单个 Job 到文件（可指定状态和发布时间） */
    private void seedJob(String jobId, String course, String moName,
                         String status, String releaseTime) throws Exception {
        java.util.List<Job> existing = new java.util.ArrayList<>(jobRepo.loadAll());
        existing.add(new Job(
                jobId, course, "Module TA", 10, moName, moName + "@bupt.edu",
                status, releaseTime, "skill", "help", "2026-9"
        ));
        jobRepo.saveAll(existing);
    }

    /** 写入单个 Application 到文件 */
    private void seedApp(String appId, String taId, String jobId,
                         String status, String comment) throws Exception {
        seedApp(appId, taId, jobId, status, comment, "2026-03-02 12:00:00");
    }

    /** 写入单个 Application 到文件（可指定申请时间） */
    private void seedApp(String appId, String taId, String jobId,
                         String status, String comment, String applicationTime) throws Exception {
        java.util.List<Application> existing = new java.util.ArrayList<>(appRepo.loadAll());
        existing.add(new Application(appId, taId, jobId, applicationTime, status, comment));
        appRepo.saveAll(existing);
    }

    /** 从文件中按 ID 查找 Job */
    private Job findJobById(String jobId) throws Exception {
        return jobRepo.loadAll().stream()
                .filter(j -> jobId.equals(j.getJobId()))
                .findFirst()
                .orElse(null);
    }

    /** 从文件中按 ID 查找 Application */
    private Application findById(String appId) throws Exception {
        return appRepo.loadAll().stream()
                .filter(a -> appId.equals(a.getApplicationId()))
                .findFirst()
                .orElse(null);
    }

    // =========================================================================
    // 正常场景
    // =========================================================================

    @Test
    @DisplayName("Review approved: status becomes Approved and comment is saved")
    void reviewApplication_approved_updatesStatusAndComment() throws Exception {
        seedJob("JOB-1", "math", "mo1");
        seedApp("APP-1", "TA-1", "JOB-1", "Pending", "");

        boolean result = moService.reviewApplication("APP-1", "mo1", "Approved", "good");

        assertTrue(result);
        Application updated = findById("APP-1");
        assertNotNull(updated);
        assertEquals("Approved", updated.getStatus());
        assertEquals("good",     updated.getReviewComment());
    }

    @Test
    @DisplayName("Review rejected: status becomes Rejected and comment is saved")
    void reviewApplication_rejected_updatesStatusAndComment() throws Exception {
        seedJob("JOB-2", "java", "mo1");
        seedApp("APP-2", "TA-2", "JOB-2", "Pending", "");

        boolean result = moService.reviewApplication("APP-2", "mo1", "Rejected", "not fit");

        assertTrue(result);
        Application updated = findById("APP-2");
        assertNotNull(updated);
        assertEquals("Rejected", updated.getStatus());
        assertEquals("not fit",  updated.getReviewComment());
    }

    // =========================================================================
    // 输入校验
    // =========================================================================

    @Test
    @DisplayName("Review fails: comment longer than 50 characters is rejected")
    void reviewApplication_commentTooLong_returnsFalse() throws Exception {
        seedJob("JOB-3", "math", "mo1");
        seedApp("APP-3", "TA-3", "JOB-3", "Pending", "");

        boolean result = moService.reviewApplication("APP-3", "mo1", "Approved", "a".repeat(51));

        assertFalse(result);
    }

    @Test
    @DisplayName("Review fails (too long comment): original status and comment unchanged")
    void reviewApplication_commentTooLong_dataUnchanged() throws Exception {
        seedJob("JOB-3", "math", "mo1");
        seedApp("APP-3", "TA-3", "JOB-3", "Pending", "");

        moService.reviewApplication("APP-3", "mo1", "Approved", "a".repeat(51));

        Application updated = findById("APP-3");
        assertNotNull(updated);
        assertEquals("Pending", updated.getStatus());
        assertEquals("",        updated.getReviewComment());
    }

    // =========================================================================
    // 边界情况
    // =========================================================================

    @Test
    @DisplayName("Review fails: application ID not found returns false")
    void reviewApplication_appNotFound_returnsFalse() throws Exception {
        seedJob("JOB-4", "math", "mo1");
        seedApp("APP-OTHER", "TA-4", "JOB-4", "Pending", "");

        boolean result = moService.reviewApplication("APP-MISSING", "mo1", "Approved", "good");

        assertFalse(result);
    }

    @Test
    @DisplayName("Review fails (app not found): existing application is unchanged")
    void reviewApplication_appNotFound_existingDataUnchanged() throws Exception {
        seedJob("JOB-4", "math", "mo1");
        seedApp("APP-OTHER", "TA-4", "JOB-4", "Pending", "");

        moService.reviewApplication("APP-MISSING", "mo1", "Approved", "good");

        Application existing = findById("APP-OTHER");
        assertNotNull(existing);
        assertEquals("Pending", existing.getStatus());
        assertEquals("",        existing.getReviewComment());
    }

    // =========================================================================
    // 权限控制
    // =========================================================================

    @Test
    @DisplayName("Review fails: MO cannot review another MO's job")
    void reviewApplication_wrongMo_returnsFalse() throws Exception {
        seedJob("JOB-5", "java", "mo2");   // 岗位属于 mo2
        seedApp("APP-5", "TA-5", "JOB-5", "Pending", "");

        boolean result = moService.reviewApplication("APP-5", "mo1", "Approved", "good");  // mo1 审核

        assertFalse(result);
    }

    @Test
    @DisplayName("Review fails (wrong MO): application status and comment unchanged")
    void reviewApplication_wrongMo_dataUnchanged() throws Exception {
        seedJob("JOB-5", "java", "mo2");
        seedApp("APP-5", "TA-5", "JOB-5", "Pending", "");

        moService.reviewApplication("APP-5", "mo1", "Approved", "good");

        Application updated = findById("APP-5");
        assertNotNull(updated);
        assertEquals("Pending", updated.getStatus());
        assertEquals("",        updated.getReviewComment());
    }

    // =========================================================================
    // 状态机约束
    // =========================================================================

    @Test
    @DisplayName("Review fails: already Approved application cannot be reviewed again")
    void reviewApplication_alreadyApproved_returnsFalse() throws Exception {
        seedJob("JOB-6", "math", "mo1");
        seedApp("APP-6", "TA-6", "JOB-6", "Approved", "old");  // 已经是 Approved

        boolean result = moService.reviewApplication("APP-6", "mo1", "Rejected", "new");

        assertFalse(result);
    }

    @Test
    @DisplayName("Review fails (not Pending): original status and comment preserved")
    void reviewApplication_alreadyApproved_originalDataPreserved() throws Exception {
        seedJob("JOB-6", "math", "mo1");
        seedApp("APP-6", "TA-6", "JOB-6", "Approved", "old");

        moService.reviewApplication("APP-6", "mo1", "Rejected", "new");

        Application updated = findById("APP-6");
        assertNotNull(updated);
        assertEquals("Approved", updated.getStatus());
        assertEquals("old",      updated.getReviewComment());
    }

    // =========================================================================
    // postJob()
    // =========================================================================

    @Nested
    @DisplayName("postJob()")
    class PostJob {

        @Test
        @DisplayName("Valid input: returns a JOB- prefixed id and persists the job")
        void postJob_valid_returnsIdAndPersists() throws Exception {
            String jobId = moService.postJob("Java", "Module TA", 10,
                    "mo1", "mo1@bupt.edu", "skillReq", "content", "2026-12");

            assertNotNull(jobId);
            assertTrue(jobId.startsWith("JOB-"), "Job id should start with JOB-");

            Job saved = findJobById(jobId);
            assertNotNull(saved);
            assertEquals("Java",       saved.getCourseName());
            assertEquals("Module TA",  saved.getPositionType());
            assertEquals(10,           saved.getWeeklyWorkload());
            assertEquals("mo1",        saved.getMoName());
            assertEquals("Recruiting", saved.getStatus(), "Newly posted job should be Recruiting");
            assertNotNull(saved.getReleaseTime());
            assertFalse(saved.getReleaseTime().isBlank());
        }

        @Test
        @DisplayName("Null courseName is rejected")
        void postJob_nullCourse_returnsNull() throws Exception {
            assertNull(moService.postJob(null, "Module TA", 10,
                    "mo1", "mo1@bupt.edu", "s", "c", "2026-12"));
            assertTrue(jobRepo.loadAll().isEmpty(), "No job should be persisted");
        }

        @Test
        @DisplayName("Blank courseName is rejected")
        void postJob_blankCourse_returnsNull() throws Exception {
            assertNull(moService.postJob("   ", "Module TA", 10,
                    "mo1", "mo1@bupt.edu", "s", "c", "2026-12"));
            assertTrue(jobRepo.loadAll().isEmpty());
        }

        @Test
        @DisplayName("Null positionType is rejected")
        void postJob_nullPosition_returnsNull() throws Exception {
            assertNull(moService.postJob("Java", null, 10,
                    "mo1", "mo1@bupt.edu", "s", "c", "2026-12"));
        }

        @Test
        @DisplayName("Zero or negative weeklyWorkload is rejected")
        void postJob_invalidWorkload_returnsNull() throws Exception {
            assertNull(moService.postJob("Java", "Module TA", 0,
                    "mo1", "mo1@bupt.edu", "s", "c", "2026-12"));
            assertNull(moService.postJob("Java", "Module TA", -5,
                    "mo1", "mo1@bupt.edu", "s", "c", "2026-12"));
        }

        @Test
        @DisplayName("Multiple posts append rather than overwrite")
        void postJob_multipleCalls_appendsJobs() throws Exception {
            moService.postJob("Java", "Module TA", 10, "mo1", "mo1@bupt.edu", "s", "c", "2026-12");
            moService.postJob("Math", "Module TA", 8,  "mo1", "mo1@bupt.edu", "s", "c", "2026-12");

            assertEquals(2, jobRepo.loadAll().size());
        }
    }

    // =========================================================================
    // getMyPostedJobs()
    // =========================================================================

    @Nested
    @DisplayName("getMyPostedJobs()")
    class GetMyPostedJobs {

        @Test
        @DisplayName("Only returns jobs owned by the given MO")
        void filtersByMoName() throws Exception {
            seedJob("JOB-A", "Java", "mo1", "Recruiting", "2026-03-01 10:00:00");
            seedJob("JOB-B", "Math", "mo2", "Recruiting", "2026-03-02 10:00:00");
            seedJob("JOB-C", "C++",  "mo1", "Recruiting", "2026-03-03 10:00:00");

            List<Job> jobs = moService.getMyPostedJobs("mo1");

            assertEquals(2, jobs.size());
            jobs.forEach(j -> assertEquals("mo1", j.getMoName()));
        }

        @Test
        @DisplayName("Sorted by releaseTime descending")
        void sortedByReleaseTimeDesc() throws Exception {
            seedJob("JOB-A", "Java", "mo1", "Recruiting", "2026-03-01 10:00:00");
            seedJob("JOB-B", "Math", "mo1", "Recruiting", "2026-03-10 10:00:00");
            seedJob("JOB-C", "C++",  "mo1", "Recruiting", "2026-03-05 10:00:00");

            List<Job> jobs = moService.getMyPostedJobs("mo1");

            assertEquals("JOB-B", jobs.get(0).getJobId(), "Newest first");
            assertEquals("JOB-C", jobs.get(1).getJobId());
            assertEquals("JOB-A", jobs.get(2).getJobId());
        }

        @Test
        @DisplayName("Closed jobs are still returned (status is not filtered)")
        void includesClosedJobs() throws Exception {
            seedJob("JOB-A", "Java", "mo1", "Closed",     "2026-03-01 10:00:00");
            seedJob("JOB-B", "Math", "mo1", "Recruiting", "2026-03-02 10:00:00");

            assertEquals(2, moService.getMyPostedJobs("mo1").size());
        }

        @Test
        @DisplayName("Returns empty when MO has no jobs")
        void unknownMo_returnsEmpty() throws Exception {
            seedJob("JOB-A", "Java", "mo1");

            assertTrue(moService.getMyPostedJobs("mo-unknown").isEmpty());
        }
    }

    // =========================================================================
    // editJob()
    // =========================================================================

    @Nested
    @DisplayName("editJob()")
    class EditJob {

        @Test
        @DisplayName("Owner edits a Recruiting job: fields are updated and persisted")
        void happyPath_fieldsUpdated() throws Exception {
            seedJob("JOB-1", "Java", "mo1");

            boolean ok = moService.editJob("JOB-1", "mo1", "Advanced Java",
                    "Course TA", 15, "skill2", "content2", "2027-01");

            assertTrue(ok);
            Job updated = findJobById("JOB-1");
            assertEquals("Advanced Java", updated.getCourseName());
            assertEquals("Course TA",     updated.getPositionType());
            assertEquals(15,              updated.getWeeklyWorkload());
            assertEquals("skill2",        updated.getSkillRequirements());
            assertEquals("content2",      updated.getJobContent());
            assertEquals("2027-01",       updated.getDeadline());
            assertEquals("Recruiting",    updated.getStatus(), "Status should be preserved");
        }

        @Test
        @DisplayName("Edit by non-owner MO is rejected, data unchanged")
        void wrongOwner_rejectedAndUnchanged() throws Exception {
            seedJob("JOB-1", "Java", "mo1");

            boolean ok = moService.editJob("JOB-1", "mo2", "Hacked",
                    "Module TA", 99, "x", "x", "x");

            assertFalse(ok);
            Job unchanged = findJobById("JOB-1");
            assertEquals("Java", unchanged.getCourseName());
            assertEquals(10,     unchanged.getWeeklyWorkload());
        }

        @Test
        @DisplayName("Edit on missing job returns false")
        void missingJob_returnsFalse() throws Exception {
            assertFalse(moService.editJob("JOB-NONE", "mo1", "X", "Module TA", 10, "s", "c", "d"));
        }

        @Test
        @DisplayName("Edit on Closed job is rejected")
        void closedJob_rejected() throws Exception {
            seedJob("JOB-1", "Java", "mo1", "Closed", "2026-03-01 10:00:00");

            assertFalse(moService.editJob("JOB-1", "mo1", "New", "Module TA", 12, "s", "c", "d"));
            assertEquals("Java", findJobById("JOB-1").getCourseName());
        }

        @Test
        @DisplayName("Invalid fields (blank/zero) are rejected")
        void invalidFields_rejected() throws Exception {
            seedJob("JOB-1", "Java", "mo1");

            assertFalse(moService.editJob("JOB-1", "mo1", "",          "Module TA", 10, "s", "c", "d"));
            assertFalse(moService.editJob("JOB-1", "mo1", "Java",      "",           10, "s", "c", "d"));
            assertFalse(moService.editJob("JOB-1", "mo1", "Java",      "Module TA",   0, "s", "c", "d"));
            assertFalse(moService.editJob("JOB-1", "mo1", null,        "Module TA", 10, "s", "c", "d"));
            assertEquals("Java", findJobById("JOB-1").getCourseName(), "Original course preserved");
        }
    }

    // =========================================================================
    // closeJob()
    // =========================================================================

    @Nested
    @DisplayName("closeJob()")
    class CloseJob {

        @Test
        @DisplayName("Owner closes a Recruiting job: status becomes Closed")
        void happyPath_statusBecomesClosed() throws Exception {
            seedJob("JOB-1", "Java", "mo1");

            boolean ok = moService.closeJob("JOB-1", "mo1");

            assertTrue(ok);
            assertEquals("Closed", findJobById("JOB-1").getStatus());
        }

        @Test
        @DisplayName("Non-owner cannot close another MO's job")
        void wrongOwner_rejected() throws Exception {
            seedJob("JOB-1", "Java", "mo1");

            assertFalse(moService.closeJob("JOB-1", "mo2"));
            assertEquals("Recruiting", findJobById("JOB-1").getStatus());
        }

        @Test
        @DisplayName("Closing a missing job returns false")
        void missingJob_returnsFalse() throws Exception {
            assertFalse(moService.closeJob("JOB-NONE", "mo1"));
        }

        @Test
        @DisplayName("Already-closed job cannot be closed again")
        void alreadyClosed_returnsFalse() throws Exception {
            seedJob("JOB-1", "Java", "mo1", "Closed", "2026-03-01 10:00:00");

            assertFalse(moService.closeJob("JOB-1", "mo1"));
        }
    }

    // =========================================================================
    // getJobApplications()
    // =========================================================================

    @Nested
    @DisplayName("getJobApplications()")
    class GetJobApplications {

        @Test
        @DisplayName("Owner sees all applications for their job, sorted desc by time")
        void happyPath_returnsSortedApplications() throws Exception {
            seedJob("JOB-1", "Java", "mo1");
            seedApp("APP-1", "TA-1", "JOB-1", "Pending",  "", "2026-04-01 10:00:00");
            seedApp("APP-2", "TA-2", "JOB-1", "Approved", "", "2026-04-03 10:00:00");
            seedApp("APP-3", "TA-3", "JOB-1", "Rejected", "", "2026-04-02 10:00:00");

            List<Application> apps = moService.getJobApplications("JOB-1", "mo1");

            assertEquals(3, apps.size());
            assertEquals("APP-2", apps.get(0).getApplicationId(), "Newest first");
            assertEquals("APP-3", apps.get(1).getApplicationId());
            assertEquals("APP-1", apps.get(2).getApplicationId());
        }

        @Test
        @DisplayName("Non-owner gets empty list (permission denied)")
        void wrongOwner_returnsEmpty() throws Exception {
            seedJob("JOB-1", "Java", "mo1");
            seedApp("APP-1", "TA-1", "JOB-1", "Pending", "");

            assertTrue(moService.getJobApplications("JOB-1", "mo2").isEmpty());
        }

        @Test
        @DisplayName("Only applications belonging to the target job are returned")
        void filtersByJobId() throws Exception {
            seedJob("JOB-1", "Java", "mo1");
            seedJob("JOB-2", "Math", "mo1");
            seedApp("APP-1", "TA-1", "JOB-1", "Pending", "");
            seedApp("APP-2", "TA-2", "JOB-2", "Pending", "");

            List<Application> apps = moService.getJobApplications("JOB-1", "mo1");

            assertEquals(1, apps.size());
            assertEquals("APP-1", apps.get(0).getApplicationId());
        }

        @Test
        @DisplayName("Missing job returns empty list")
        void missingJob_returnsEmpty() throws Exception {
            assertTrue(moService.getJobApplications("JOB-NONE", "mo1").isEmpty());
        }
    }

    // =========================================================================
    // getJobById()
    // =========================================================================

    @Nested
    @DisplayName("getJobById()")
    class GetJobById {

        @Test
        @DisplayName("Returns job when id exists")
        void existingId_returnsJob() throws Exception {
            seedJob("JOB-1", "Java", "mo1");

            Job j = moService.getJobById("JOB-1");

            assertNotNull(j);
            assertEquals("Java", j.getCourseName());
        }

        @Test
        @DisplayName("Returns null when id does not exist")
        void missingId_returnsNull() throws Exception {
            seedJob("JOB-1", "Java", "mo1");
            assertNull(moService.getJobById("JOB-NONE"));
        }
    }
}