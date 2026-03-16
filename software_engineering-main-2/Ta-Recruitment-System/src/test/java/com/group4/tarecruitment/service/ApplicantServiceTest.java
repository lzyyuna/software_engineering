package com.group4.tarecruitment.service;

import com.group4.tarecruitment.model.Applicant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicantServiceTest {

    @Test
    void shouldAddApplicantSuccessfully() {
        ApplicantService service = new ApplicantService();
        Applicant applicant = new Applicant("A001", "Alice", "alice@test.com", "Java,Communication");

        service.addApplicant(applicant);

        assertEquals(1, service.getAllApplicants().size());
        assertEquals("Alice", service.getAllApplicants().get(0).getName());
    }
}