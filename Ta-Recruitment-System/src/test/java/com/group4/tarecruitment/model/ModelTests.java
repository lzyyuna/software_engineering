package com.group4.tarecruitment.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for plain model classes (POJOs).
 *
 * Models contain no business logic, so we focus on:
 *   - constructor wiring (all fields assigned correctly)
 *   - default / fallback values
 *   - setter / getter symmetry
 *   - any helper aliases that production code relies on
 */
@DisplayName("Domain Model Unit Tests")
class ModelTests {

    // =========================================================================
    // Job
    // =========================================================================

    @Nested
    @DisplayName("Job")
    class JobTests {

        @Test
        @DisplayName("Full 12-arg constructor sets every field")
        void fullConstructor_assignsAllFields() {
            Job j = new Job("JOB-1", "Java", "Module TA", 10,
                    "mo1", "mo1@bupt.edu", "Recruiting", "2026-03-01 10:00:00",
                    "skill", "content", "2026-12", "Engineering");

            assertEquals("JOB-1",      j.getJobId());
            assertEquals("Java",       j.getCourseName());
            assertEquals("Module TA",  j.getPositionType());
            assertEquals(10,           j.getWeeklyWorkload());
            assertEquals("mo1",        j.getMoName());
            assertEquals("mo1@bupt.edu", j.getMoEmail());
            assertEquals("Recruiting", j.getStatus());
            assertEquals("2026-03-01 10:00:00", j.getReleaseTime());
            assertEquals("skill",      j.getSkillRequirements());
            assertEquals("content",    j.getJobContent());
            assertEquals("2026-12",    j.getDeadline());
            assertEquals("Engineering", j.getDepartment());
        }

        @Test
        @DisplayName("Compatibility 11-arg constructor defaults department to 'General/Others'")
        void compatibilityConstructor_defaultsDepartment() {
            Job j = new Job("JOB-1", "Java", "Module TA", 10,
                    "mo1", "mo1@bupt.edu", "Recruiting", "2026-03-01 10:00:00",
                    "skill", "content", "2026-12");

            assertEquals("General/Others", j.getDepartment());
        }

        @Test
        @DisplayName("No-arg constructor leaves fields null / 0")
        void noArgConstructor_defaults() {
            Job j = new Job();
            assertNull(j.getJobId());
            assertNull(j.getCourseName());
            assertEquals(0, j.getWeeklyWorkload());
        }

        @Test
        @DisplayName("Setters update fields")
        void setters_updateFields() {
            Job j = new Job();
            j.setJobId("JOB-X");
            j.setCourseName("Math");
            j.setPositionType("Course TA");
            j.setWeeklyWorkload(7);
            j.setMoName("mo2");
            j.setMoEmail("mo2@bupt.edu");
            j.setStatus("Closed");
            j.setReleaseTime("t");
            j.setSkillRequirements("s");
            j.setJobContent("c");
            j.setDeadline("d");
            j.setDepartment("Science");

            assertEquals("JOB-X",     j.getJobId());
            assertEquals("Math",      j.getCourseName());
            assertEquals("Course TA", j.getPositionType());
            assertEquals(7,           j.getWeeklyWorkload());
            assertEquals("mo2",       j.getMoName());
            assertEquals("mo2@bupt.edu", j.getMoEmail());
            assertEquals("Closed",    j.getStatus());
            assertEquals("t",         j.getReleaseTime());
            assertEquals("s",         j.getSkillRequirements());
            assertEquals("c",         j.getJobContent());
            assertEquals("d",         j.getDeadline());
            assertEquals("Science",   j.getDepartment());
        }
    }

    // =========================================================================
    // Application
    // =========================================================================

    @Nested
    @DisplayName("Application")
    class ApplicationTests {

        @Test
        @DisplayName("Full constructor sets every field")
        void fullConstructor_assignsAllFields() {
            Application a = new Application("APP-1", "TA-1", "JOB-1",
                    "2026-04-01 10:00:00", "Pending", "comment");

            assertEquals("APP-1",   a.getApplicationId());
            assertEquals("TA-1",    a.getTaId());
            assertEquals("JOB-1",   a.getJobId());
            assertEquals("2026-04-01 10:00:00", a.getApplicationTime());
            assertEquals("Pending", a.getStatus());
            assertEquals("comment", a.getReviewComment());
        }

        @Test
        @DisplayName("Setters update fields")
        void setters_updateFields() {
            Application a = new Application();
            a.setApplicationId("APP-2");
            a.setTaId("TA-2");
            a.setJobId("JOB-2");
            a.setApplicationTime("t");
            a.setStatus("Approved");
            a.setReviewComment("good");

            assertEquals("APP-2",    a.getApplicationId());
            assertEquals("TA-2",     a.getTaId());
            assertEquals("JOB-2",    a.getJobId());
            assertEquals("t",        a.getApplicationTime());
            assertEquals("Approved", a.getStatus());
            assertEquals("good",     a.getReviewComment());
        }
    }

    // =========================================================================
    // Applicant
    // =========================================================================

    @Nested
    @DisplayName("Applicant")
    class ApplicantTests {

        @Test
        @DisplayName("7-arg constructor populates basic fields and defaults password/resumePath")
        void shortConstructor_defaultsPasswordAndResumePath() {
            Applicant a = new Applicant("TA-1", "S001", "Alice", "alice@test.com",
                    "Java,Math", "Java,Comm", "123456");

            assertEquals("TA-1",            a.getTaId());
            assertEquals("S001",            a.getStudentId());
            assertEquals("Alice",           a.getName());
            assertEquals("alice@test.com",  a.getEmail());
            assertEquals("Java,Math",       a.getCourses());
            assertEquals("Java,Comm",       a.getSkillTags());
            assertEquals("123456",          a.getContact());
            assertEquals("",                a.getPassword(), "Password defaults to empty string");
            assertEquals("",                a.getResumePath(), "Resume path defaults to empty string");
            assertNull(a.getUsername(),     "Username is null when not passed");
        }

        @Test
        @DisplayName("8-arg constructor additionally wires username")
        void longConstructor_setsUsername() {
            Applicant a = new Applicant("TA-1", "S001", "Alice", "alice@test.com",
                    "Java", "Java", "123456", "user1");

            assertEquals("user1", a.getUsername());
        }

        @Test
        @DisplayName("Alias getters mirror underlying fields")
        void aliasGetters_returnSameValue() {
            Applicant a = new Applicant("TA-1", "S001", "Alice", "a@b",
                    "Java", "Java,Comm", "contactX");
            a.setResumePath("/tmp/cv.pdf");

            assertEquals(a.getContact(),    a.getPhone(),  "getPhone() aliases getContact()");
            assertEquals(a.getSkillTags(),  a.getSkills(), "getSkills() aliases getSkillTags()");
            assertEquals(a.getResumePath(), a.getCvPath(), "getCvPath() aliases getResumePath()");
        }

        @Test
        @DisplayName("Setters update all mutable fields")
        void setters_updateFields() {
            Applicant a = new Applicant();
            a.setTaId("TA-1");
            a.setStudentId("S001");
            a.setName("Bob");
            a.setEmail("bob@x");
            a.setCourses("Math");
            a.setSkillTags("Math");
            a.setContact("999");
            a.setPassword("pw");
            a.setResumePath("/r");
            a.setUsername("u");

            assertEquals("TA-1", a.getTaId());
            assertEquals("S001", a.getStudentId());
            assertEquals("Bob",  a.getName());
            assertEquals("bob@x", a.getEmail());
            assertEquals("Math", a.getCourses());
            assertEquals("Math", a.getSkillTags());
            assertEquals("999",  a.getContact());
            assertEquals("pw",   a.getPassword());
            assertEquals("/r",   a.getResumePath());
            assertEquals("u",    a.getUsername());
        }
    }

    // =========================================================================
    // InviteCode
    // =========================================================================

    @Nested
    @DisplayName("InviteCode")
    class InviteCodeTests {

        @Test
        @DisplayName("Full constructor sets every field")
        void fullConstructor_setsFields() {
            InviteCode c = new InviteCode("ABCD1234", "TA", false, "2026-04-01");

            assertEquals("ABCD1234",  c.getCode());
            assertEquals("TA",        c.getRole());
            assertFalse(c.isUsed());
            assertEquals("2026-04-01", c.getCreatedAt());
        }

        @Test
        @DisplayName("Setters mutate state, including the 'used' flag")
        void setters_updateFields() {
            InviteCode c = new InviteCode();
            c.setCode("X");
            c.setRole("MO");
            c.setUsed(true);
            c.setCreatedAt("today");

            assertEquals("X",     c.getCode());
            assertEquals("MO",    c.getRole());
            assertTrue(c.isUsed());
            assertEquals("today", c.getCreatedAt());
        }
    }

    // =========================================================================
    // User
    // =========================================================================

    @Nested
    @DisplayName("User")
    class UserTests {

        @Test
        @DisplayName("Constructor exposes fields read-only via getters")
        void constructor_setsFields() {
            User u = new User("ta001", "secret", "TA");

            assertEquals("ta001",  u.getUsername());
            assertEquals("secret", u.getPassword());
            assertEquals("TA",     u.getRole());
        }
    }
}
