package com.group4.tarecruitment.service;

import com.group4.tarecruitment.model.Admin;
import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.model.Application;
import com.group4.tarecruitment.model.Job;
import com.group4.tarecruitment.model.User;
import com.group4.tarecruitment.repository.AdminRepository;
import com.group4.tarecruitment.repository.ApplicationRepository;
import com.group4.tarecruitment.repository.JobRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminServiceImpl.
 *
 * ?????
 *   1. ?? @ExtendWith(MockitoExtension.class)?? @Mock ?????
 *   2. ???????? Mock??????? "new" ? @InjectMocks?
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Unit Tests")
class AdminServiceTest {

    // ?? Mocks ????????????????????????????????????????????????????????????????
    @Mock private AdminRepository       adminRepository;
    @Mock private JobRepository         jobRepository;
    @Mock private ApplicationRepository appRepository;
    @Mock private ApplicantService      applicantService;

    /** ???????????????? mock?????? */
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        // ??????????? mock —— ???????
        adminService = new AdminServiceImpl(
                adminRepository, jobRepository, appRepository, applicantService
        );
    }

    // ?? ?????? ?????????????????????????????????????????????????????????

    private Job makeJob(String jobId, String course, double workload, String mo, String status) {
        Job j = new Job();
        j.setJobId(jobId);
        j.setCourseName(course);
        j.setWeeklyWorkload((int) workload);
        j.setMoName(mo);
        j.setStatus(status);
        j.setPositionType("Module TA");
        j.setDepartment("Engineering");
        return j;
    }

    private Application makeApp(String appId, String taId, String jobId, String status) {
        Application a = new Application();
        a.setApplicationId(appId);
        a.setTaId(taId);
        a.setJobId(jobId);
        a.setStatus(status);
        a.setApplicationTime("2026-04-01 10:00:00");
        return a;
    }

    private Applicant makeApplicant(String taId, String name) {
        Applicant a = new Applicant();
        a.setTaId(taId);
        a.setName(name);
        return a;
    }

    private User makeUser(String username, String password, String role) {
        return new User(username, password, role);
    }

    // =========================================================================
    // 1. getTaWorkload()
    // =========================================================================

    @Nested
    @DisplayName("getTaWorkload()")
    class GetTaWorkload {

        @Test
        @DisplayName("Returns correct Admin records for approved applications")
        void happyPath_returnsApprovedWorkloadRecords() throws Exception {
            List<Application> apps = List.of(
                    makeApp("APP-001", "TA-001", "JOB-001", "Approved"),
                    makeApp("APP-002", "TA-002", "JOB-002", "Approved")
            );
            List<Job> jobs = List.of(
                    makeJob("JOB-001", "Java Programming", 20, "mo001", "Recruiting"),
                    makeJob("JOB-002", "Database Systems", 15, "mo002", "Recruiting")
            );
            List<Applicant> applicants = List.of(
                    makeApplicant("TA-001", "Zhang San"),
                    makeApplicant("TA-002", "Li Si")
            );

            when(appRepository.loadAll()).thenReturn(apps);
            when(jobRepository.loadAll()).thenReturn(jobs);
            when(applicantService.getAllApplicants()).thenReturn(applicants);

            List<Admin> result = adminService.getTaWorkload();

            assertEquals(2, result.size());

            Admin r1 = result.stream()
                    .filter(a -> "TA-001".equals(a.getTaId()))
                    .findFirst()
                    .orElseThrow();
            assertEquals("Zhang San", r1.getTaName());
            assertEquals("Java Programming", r1.getCourseName());
            assertEquals(20.0, r1.getWeeklyWorkload(), 0.001);
            assertEquals(20.0, r1.getTotalWorkload(), 0.001);
            assertEquals("mo001", r1.getHireMo());
        }

        @Test
        @DisplayName("Pending or Rejected applications are excluded")
        void onlyApprovedApplicationsAreIncluded() throws Exception {
            List<Application> apps = List.of(
                    makeApp("APP-001", "TA-001", "JOB-001", "Pending"),
                    makeApp("APP-002", "TA-002", "JOB-002", "Rejected"),
                    makeApp("APP-003", "TA-003", "JOB-001", "Approved")
            );
            List<Job> jobs = List.of(makeJob("JOB-001", "Java Programming", 20, "mo001", "Recruiting"));
            List<Applicant> applicants = List.of(makeApplicant("TA-003", "Wang Wu"));

            when(appRepository.loadAll()).thenReturn(apps);
            when(jobRepository.loadAll()).thenReturn(jobs);
            when(applicantService.getAllApplicants()).thenReturn(applicants);

            List<Admin> result = adminService.getTaWorkload();

            assertEquals(1, result.size());
            assertEquals("TA-003", result.get(0).getTaId());
        }

        @Test
        @DisplayName("Returns empty list when there are no applications")
        void emptyApplications_returnsEmptyList() throws Exception {
            when(appRepository.loadAll()).thenReturn(new ArrayList<>());
            when(jobRepository.loadAll()).thenReturn(new ArrayList<>());
            when(applicantService.getAllApplicants()).thenReturn(new ArrayList<>());

            List<Admin> result = adminService.getTaWorkload();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Skips record if matching Job is not found")
        void missingJob_recordSkipped() throws Exception {
            List<Application> apps = List.of(makeApp("APP-001", "TA-001", "JOB-999", "Approved"));
            List<Job> jobs = List.of(makeJob("JOB-001", "Java", 20, "mo001", "Recruiting"));
            List<Applicant> applicants = List.of(makeApplicant("TA-001", "Zhang San"));

            when(appRepository.loadAll()).thenReturn(apps);
            when(jobRepository.loadAll()).thenReturn(jobs);
            when(applicantService.getAllApplicants()).thenReturn(applicants);

            List<Admin> result = adminService.getTaWorkload();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Skips record if matching Applicant is not found")
        void missingApplicant_recordSkipped() throws Exception {
            List<Application> apps = List.of(makeApp("APP-001", "TA-999", "JOB-001", "Approved"));
            List<Job> jobs = List.of(makeJob("JOB-001", "Java", 20, "mo001", "Recruiting"));
            List<Applicant> applicants = List.of(makeApplicant("TA-001", "Zhang San"));

            when(appRepository.loadAll()).thenReturn(apps);
            when(jobRepository.loadAll()).thenReturn(jobs);
            when(applicantService.getAllApplicants()).thenReturn(applicants);

            List<Admin> result = adminService.getTaWorkload();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("TotalWorkload accumulates all approved jobs for the same TA")
        void totalWorkload_accumulatesAcrossMultipleJobs() throws Exception {
            List<Application> apps = List.of(
                    makeApp("APP-001", "TA-001", "JOB-001", "Approved"),
                    makeApp("APP-002", "TA-001", "JOB-002", "Approved")
            );
            List<Job> jobs = List.of(
                    makeJob("JOB-001", "Java Programming", 20, "mo001", "Recruiting"),
                    makeJob("JOB-002", "Database Systems", 15, "mo001", "Recruiting")
            );
            List<Applicant> applicants = List.of(makeApplicant("TA-001", "Zhang San"));

            when(appRepository.loadAll()).thenReturn(apps);
            when(jobRepository.loadAll()).thenReturn(jobs);
            when(applicantService.getAllApplicants()).thenReturn(applicants);

            List<Admin> result = adminService.getTaWorkload();

            assertEquals(2, result.size());
            result.forEach(r -> assertEquals(35.0, r.getTotalWorkload(), 0.001));
        }

        @Test
        @DisplayName("Department defaults to 'General/Others' when job has no department")
        void nullDepartment_defaultsToGeneralOthers() throws Exception {
            Job jobNoDept = makeJob("JOB-001", "Java", 20, "mo001", "Recruiting");
            jobNoDept.setDepartment(null);

            List<Application> apps = List.of(makeApp("APP-001", "TA-001", "JOB-001", "Approved"));
            List<Applicant> applicants = List.of(makeApplicant("TA-001", "Zhang San"));

            when(appRepository.loadAll()).thenReturn(apps);
            when(jobRepository.loadAll()).thenReturn(List.of(jobNoDept));
            when(applicantService.getAllApplicants()).thenReturn(applicants);

            List<Admin> result = adminService.getTaWorkload();

            assertEquals(1, result.size());
            assertEquals("General/Others", result.get(0).getDepartment());
        }

        @Test
        @DisplayName("Propagates exception when ApplicationRepository throws")
        void repositoryException_propagated() throws Exception {
            when(appRepository.loadAll()).thenThrow(new RuntimeException("IO error"));

            assertThrows(RuntimeException.class, () -> adminService.getTaWorkload());
        }
    }

    // =========================================================================
    // 2. getAllUsers()
    // =========================================================================

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("Returns all users from repository")
        void happyPath_returnsUsers() throws Exception {
            List<User> users = List.of(
                    makeUser("ta001",    "123456", "TA"),
                    makeUser("mo001",    "123456", "MO"),
                    makeUser("admin001", "123456", "Admin")
            );
            when(adminRepository.loadAllUsers()).thenReturn(users);

            List<User> result = adminService.getAllUsers();

            assertEquals(3, result.size());
            assertTrue(result.stream().anyMatch(u -> "Admin".equals(u.getRole())));
        }

        @Test
        @DisplayName("Returns empty list when no users exist")
        void noUsers_returnsEmptyList() throws Exception {
            when(adminRepository.loadAllUsers()).thenReturn(new ArrayList<>());

            List<User> result = adminService.getAllUsers();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Propagates exception from repository")
        void repositoryException_propagated() throws Exception {
            when(adminRepository.loadAllUsers()).thenThrow(new Exception("File not found"));

            assertThrows(Exception.class, () -> adminService.getAllUsers());
        }
    }

    // =========================================================================
    // 3. getAllJobs()
    // =========================================================================

    @Nested
    @DisplayName("getAllJobs()")
    class GetAllJobs {

        @Test
        @DisplayName("Returns all jobs from JobRepository")
        void happyPath_returnsJobs() throws Exception {
            List<Job> jobs = List.of(
                    makeJob("JOB-001", "Java Programming", 20, "mo001", "Recruiting"),
                    makeJob("JOB-002", "Database Systems", 15, "mo002", "Closed")
            );
            when(jobRepository.loadAll()).thenReturn(jobs);

            List<Job> result = adminService.getAllJobs();

            assertEquals(2, result.size());
            assertEquals("JOB-001", result.get(0).getJobId());
        }

        @Test
        @DisplayName("Returns empty list when no jobs exist")
        void noJobs_returnsEmptyList() throws Exception {
            when(jobRepository.loadAll()).thenReturn(new ArrayList<>());

            List<Job> result = adminService.getAllJobs();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Propagates exception from JobRepository")
        void repositoryException_propagated() throws Exception {
            when(jobRepository.loadAll()).thenThrow(new Exception("CSV parse error"));

            assertThrows(Exception.class, () -> adminService.getAllJobs());
        }
    }

    // =========================================================================
    // 4. updateJobStatus()
    // =========================================================================

    @Nested
    @DisplayName("updateJobStatus()")
    class UpdateJobStatus {

        @Test
        @DisplayName("Delegates to AdminRepository with correct arguments")
        void happyPath_delegatesToRepository() throws Exception {
            doNothing().when(adminRepository).updateJobStatus("JOB-001", "Closed");

            adminService.updateJobStatus("JOB-001", "Closed");

            verify(adminRepository, times(1)).updateJobStatus("JOB-001", "Closed");
        }

        @Test
        @DisplayName("Propagates exception when repository throws")
        void repositoryException_propagated() throws Exception {
            doThrow(new Exception("Write failed"))
                    .when(adminRepository).updateJobStatus(anyString(), anyString());

            assertThrows(Exception.class, () -> adminService.updateJobStatus("JOB-001", "Closed"));
        }

        @Test
        @DisplayName("Handles various valid status values without error")
        void variousStatuses_acceptedWithoutError() throws Exception {
            String[] statuses = {"Recruiting", "Closed", "Pending", "Cancelled"};
            for (String s : statuses) {
                doNothing().when(adminRepository).updateJobStatus("JOB-001", s);
                assertDoesNotThrow(() -> adminService.updateJobStatus("JOB-001", s));
            }
        }
    }

    // =========================================================================
    // 5. exportTaWorkload() / exportTaWorkloadData()
    // =========================================================================

    @Nested
    @DisplayName("exportTaWorkload()")
    class ExportTaWorkload {

        @Test
        @DisplayName("Returns a non-null, non-empty export file path")
        void happyPath_returnsExportPath() throws Exception {
            when(appRepository.loadAll()).thenReturn(new ArrayList<>());
            when(jobRepository.loadAll()).thenReturn(new ArrayList<>());
            when(applicantService.getAllApplicants()).thenReturn(new ArrayList<>());

            String path = adminService.exportTaWorkload();

            assertNotNull(path);
            assertFalse(path.isBlank());
        }

        @Test
        @DisplayName("exportTaWorkloadData() writes a CSV file with data")
        void exportTaWorkloadData_writesCsvFile(@TempDir Path dir) throws Exception {
            Admin record = new Admin("TA-001", "Zhang San", "mo001", "Module TA",
                    "Java Programming", "Engineering", 20.0, 20.0, "2026-04-01");

            String exportPath = dir.resolve("export_test.csv").toString();
            adminService.exportTaWorkloadData(List.of(record), exportPath);

            File exported = new File(exportPath);
            assertTrue(exported.exists(), "Export file should be created");
            assertTrue(exported.length() > 0, "Export file should not be empty");
        }

        @Test
        @DisplayName("exportTaWorkloadData() with empty list still writes headers")
        void exportTaWorkloadData_emptyList_writesHeadersOnly(@TempDir Path dir) throws Exception {
            String exportPath = dir.resolve("empty_export.csv").toString();
            adminService.exportTaWorkloadData(new ArrayList<>(), exportPath);

            File exported = new File(exportPath);
            assertTrue(exported.exists());
            assertTrue(exported.length() > 0, "Header row should still be written");
        }
    }

    // =========================================================================
    // 6. printTaWorkloadReport()
    // =========================================================================

    @Nested
    @DisplayName("printTaWorkloadReport()")
    class PrintTaWorkloadReport {

        @Test
        @DisplayName("Completes without throwing when data is available")
        void happyPath_noException() throws Exception {
            List<Application> apps = List.of(makeApp("APP-001", "TA-001", "JOB-001", "Approved"));
            List<Job> jobs = List.of(makeJob("JOB-001", "Java", 20, "mo001", "Recruiting"));
            List<Applicant> applicants = List.of(makeApplicant("TA-001", "Zhang San"));

            when(appRepository.loadAll()).thenReturn(apps);
            when(jobRepository.loadAll()).thenReturn(jobs);
            when(applicantService.getAllApplicants()).thenReturn(applicants);

            assertDoesNotThrow(() -> adminService.printTaWorkloadReport());
        }

        @Test
        @DisplayName("Completes without throwing when there is no data")
        void emptyData_noException() throws Exception {
            when(appRepository.loadAll()).thenReturn(new ArrayList<>());
            when(jobRepository.loadAll()).thenReturn(new ArrayList<>());
            when(applicantService.getAllApplicants()).thenReturn(new ArrayList<>());

            assertDoesNotThrow(() -> adminService.printTaWorkloadReport());
        }
    }

    // =========================================================================
    // 7. Admin Model
    // =========================================================================

    @Nested
    @DisplayName("Admin model")
    class AdminModelTests {

        @Test
        @DisplayName("No-arg constructor initialises object without throwing")
        void noArgConstructor_works() {
            assertDoesNotThrow(() -> new Admin());
        }

        @Test
        @DisplayName("Full constructor sets all fields correctly")
        void fullConstructor_setsFields() {
            Admin a = new Admin("TA-001", "Zhang San", "mo001", "Module TA",
                    "Java Programming", "Engineering", 20.0, 35.0, "2026-04-01");

            assertEquals("TA-001", a.getTaId());
            assertEquals("Zhang San", a.getTaName());
            assertEquals("mo001", a.getHireMo());
            assertEquals("Module TA", a.getPositionName());
            assertEquals("Java Programming", a.getCourseName());
            assertEquals("Engineering", a.getDepartment());
            assertEquals(20.0, a.getWeeklyWorkload(), 0.001);
            assertEquals(35.0, a.getTotalWorkload(), 0.001);
            assertEquals("2026-04-01", a.getHireDate());
            assertEquals(0.0, a.getExcessAmount(), 0.001);
            assertEquals("", a.getSuggestion());
        }

        @Test
        @DisplayName("Setters update fields correctly")
        void setters_updateFields() {
            Admin a = new Admin();
            a.setTaId("TA-002");
            a.setTaName("Li Si");
            a.setExcessAmount(5.0);
            a.setSuggestion("Reduce workload");

            assertEquals("TA-002", a.getTaId());
            assertEquals("Li Si", a.getTaName());
            assertEquals(5.0, a.getExcessAmount(), 0.001);
            assertEquals("Reduce workload", a.getSuggestion());
        }

        @Test
        @DisplayName("toString() contains key field values")
        void toString_containsKeyFields() {
            Admin a = new Admin("TA-001", "Zhang San", "mo001", "Module TA",
                    "Java Programming", "Engineering", 20.0, 20.0, "2026-04-01");

            String str = a.toString();
            assertTrue(str.contains("TA-001"));
            assertTrue(str.contains("Zhang San"));
            assertTrue(str.contains("Java Programming"));
            assertTrue(str.contains("20.0"));
        }
    }
}