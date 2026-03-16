package com.group4.tarecruitment.service;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.repository.ApplicantCsvRepository;
import com.group4.tarecruitment.repository.ApplicantJsonRepository;

import java.util.List;

public class ApplicantService {

    private final ApplicantCsvRepository csvRepo = new ApplicantCsvRepository();
    private final ApplicantJsonRepository jsonRepo = new ApplicantJsonRepository();

    public void addApplicant(Applicant applicant) throws Exception {
        csvRepo.save(applicant);
        jsonRepo.save(applicant);
    }

    public List<Applicant> getAllApplicants() throws Exception {
        return csvRepo.loadAll();
    }
}