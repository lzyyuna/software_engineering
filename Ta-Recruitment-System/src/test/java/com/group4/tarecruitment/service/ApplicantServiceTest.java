package com.group4.tarecruitment.service;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.repository.ApplicantCsvRepository;
import com.group4.tarecruitment.repository.ApplicantJsonRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ApplicantService.
 *
 * 为什么用集成测试而不是 Mock：
 *   ApplicantService 的核心职责就是把数据持久化到 CSV + JSON 两个文件。
 *   如果 Mock 掉文件操作，就无法验证"字段顺序、编码、双文件一致性"等真实问题。
 *
 * 优化点：
 *   1. 用 @TempDir 替代手动删除文件，测试结束后自动清理，互不干扰
 *   2. 用 @BeforeEach 统一初始化 service，避免每个测试重复构造
 *   3. 提取公共断言方法，减少重复代码
 *   4. 每个测试只验证一件事，职责更清晰
 */
@DisplayName("ApplicantService Integration Tests")
class ApplicantServiceTest {

    @TempDir
    Path tempDir;

    private ApplicantService service;
    private Path csvPath;
    private Path jsonPath;

    @BeforeEach
    void setUp() throws IOException {
        // 用临时目录隔离每个测试的文件，测试结束后 JUnit 自动删除
        csvPath  = tempDir.resolve("applicants.csv");
        jsonPath = tempDir.resolve("applicants.json");

        // 将 service 指向临时目录（需要 ApplicantService 支持自定义路径的构造器）
        service = new ApplicantService(csvPath, jsonPath);
    }

    // ── 辅助方法 ─────────────────────────────────────────────────────────────

    /** 构造一个完整的测试用申请者对象 */
    private Applicant buildApplicant(String taId, String studentId, String name,
                                     String email, String username) {
        Applicant a = new Applicant(taId, studentId, name, email,
                "Java", "Java,Communication", "123456");
        a.setUsername(username);
        return a;
    }

    /** 断言申请者基本字段正确 */
    private void assertApplicantFields(Applicant actual, String name, String email,
                                       String courses, String skills, String contact) {
        assertNotNull(actual);
        assertEquals(name,    actual.getName());
        assertEquals(email,   actual.getEmail());
        assertEquals(courses, actual.getCourses());
        assertEquals(skills,  actual.getSkillTags());
        assertEquals(contact, actual.getContact());
    }

    // =========================================================================
    // shouldAddApplicantSuccessfully
    // =========================================================================

    @Test
    @DisplayName("Add applicant: in-memory list is updated")
    void addApplicant_memoryListUpdated() throws Exception {
        service.addApplicant(buildApplicant("TA-1", "S001", "Alice", "alice@test.com", "user1"));

        List<Applicant> all = service.getAllApplicants();
        assertEquals(1, all.size());
        assertEquals("Alice", all.get(0).getName());
    }

    @Test
    @DisplayName("Add applicant: queryable by studentId with correct fields")
    void addApplicant_queryableByStudentId() throws Exception {
        service.addApplicant(buildApplicant("TA-1", "S001", "Alice", "alice@test.com", "user1"));

        Applicant found = service.getApplicantByStudentId("S001");
        assertApplicantFields(found, "Alice", "alice@test.com", "Java", "Java,Communication", "123456");
    }

    @Test
    @DisplayName("Add applicant: queryable by username")
    void addApplicant_queryableByUsername() throws Exception {
        service.addApplicant(buildApplicant("TA-1", "S001", "Alice", "alice@test.com", "user1"));

        Applicant found = service.getApplicantByUsername("user1");
        assertNotNull(found);
        assertEquals("S001", found.getStudentId());
    }

    @Test
    @DisplayName("Add applicant: persisted to CSV correctly")
    void addApplicant_persistedToCsv() throws Exception {
        service.addApplicant(buildApplicant("TA-1", "S001", "Alice", "alice@test.com", "user1"));

        ApplicantCsvRepository csvRepo = new ApplicantCsvRepository(csvPath);
        List<Applicant> csvList = csvRepo.loadAll();
        assertEquals(1, csvList.size());
        assertEquals("TA-1",  csvList.get(0).getTaId());
        assertEquals("Alice", csvList.get(0).getName());
    }

    @Test
    @DisplayName("Add applicant: persisted to JSON correctly")
    void addApplicant_persistedToJson() throws Exception {
        service.addApplicant(buildApplicant("TA-1", "S001", "Alice", "alice@test.com", "user1"));

        ApplicantJsonRepository jsonRepo = new ApplicantJsonRepository(jsonPath);
        List<Applicant> jsonList = jsonRepo.loadAll();
        assertEquals(1, jsonList.size());
        assertEquals("TA-1",              jsonList.get(0).getTaId());
        assertEquals("Alice",             jsonList.get(0).getName());
        assertEquals("alice@test.com",    jsonList.get(0).getEmail());
    }

    // =========================================================================
    // shouldUpdateApplicantSuccessfully
    // =========================================================================

    @Test
    @DisplayName("Update applicant: fields updated in memory")
    void updateApplicant_fieldsUpdatedInMemory() throws Exception {
        service.addApplicant(buildApplicant("TA-2", "S002", "Bob", "bob@test.com", "user2"));

        Applicant updated = new Applicant("TA-2", "S002", "Bobby", "bobby@test.com",
                "Python,Java", "Python,Java", "222");
        updated.setUsername("user2");
        service.updateApplicant(updated);

        Applicant found = service.getApplicantByStudentId("S002");
        assertApplicantFields(found, "Bobby", "bobby@test.com", "Python,Java", "Python,Java", "222");
    }

    @Test
    @DisplayName("Update applicant: no duplicate record created")
    void updateApplicant_noDuplicateRecord() throws Exception {
        service.addApplicant(buildApplicant("TA-2", "S002", "Bob", "bob@test.com", "user2"));

        Applicant updated = new Applicant("TA-2", "S002", "Bobby", "bobby@test.com",
                "Python,Java", "Python,Java", "222");
        updated.setUsername("user2");
        service.updateApplicant(updated);

        // 更新后记录数应仍为 1
        assertEquals(1, service.getAllApplicants().size());
    }

    @Test
    @DisplayName("Update applicant: JSON file reflects updated data")
    void updateApplicant_jsonFileUpdated() throws Exception {
        service.addApplicant(buildApplicant("TA-2", "S002", "Bob", "bob@test.com", "user2"));

        Applicant updated = new Applicant("TA-2", "S002", "Bobby", "bobby@test.com",
                "Python,Java", "Python,Java", "222");
        updated.setUsername("user2");
        service.updateApplicant(updated);

        ApplicantJsonRepository jsonRepo = new ApplicantJsonRepository(jsonPath);
        List<Applicant> jsonList = jsonRepo.loadAll();
        assertEquals(1, jsonList.size());
        assertEquals("Bobby",          jsonList.get(0).getName());
        assertEquals("bobby@test.com", jsonList.get(0).getEmail());
    }
}