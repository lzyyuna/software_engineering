package com.group4.tarecruitment.service;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.repository.ApplicantCsvRepository;
import com.group4.tarecruitment.repository.ApplicantJsonRepository;

import java.nio.file.Path;
import java.util.List;

/**
 * Coordinates TA applicant profile persistence across CSV and JSON storage.
 */
public class ApplicantService {
    private final ApplicantCsvRepository csvRepo;
    private final ApplicantJsonRepository jsonRepo;

    /**
     * Creates a service using the default applicant data files.
     */
    public ApplicantService() {
        this.csvRepo  = new ApplicantCsvRepository();
        this.jsonRepo = new ApplicantJsonRepository();
    }

    /**
     * Creates a service using custom storage paths.
     *
     * @param csvPath path to the applicant CSV file
     * @param jsonPath path to the applicant JSON file
     */
    public ApplicantService(Path csvPath, Path jsonPath) {
        this.csvRepo  = new ApplicantCsvRepository(csvPath);
        this.jsonRepo = new ApplicantJsonRepository(jsonPath);
    }

    /**
     * Loads all TA applicant profiles.
     *
     * @return all applicants from CSV storage
     * @throws Exception if the storage cannot be read
     */
    public List<Applicant> getAllApplicants() throws Exception {
        return csvRepo.loadAll();
    }

    /**
     * Finds an applicant by student ID.
     *
     * @param studentId student identifier
     * @return matching applicant, or null when not found
     * @throws Exception if the storage cannot be read
     */
    public Applicant getApplicantByStudentId(String studentId) throws Exception {
        return csvRepo.findByStudentId(studentId);
    }

    /**
     * Adds a new applicant to both CSV and JSON storage.
     *
     * @param applicant applicant profile to save
     * @throws Exception if persistence fails
     */
    public void addApplicant(Applicant applicant) throws Exception {
        csvRepo.save(applicant);
        jsonRepo.save(applicant);
    }

    /**
     * Updates an existing applicant in both CSV and JSON storage.
     *
     * @param applicant applicant profile to update
     * @throws Exception if persistence fails
     */
    public void updateApplicant(Applicant applicant) throws Exception {
        csvRepo.update(applicant);
        jsonRepo.update(applicant);
    }

    /**
     * Finds an applicant by login username.
     *
     * @param username login account username
     * @return matching applicant, or null when not found
     * @throws Exception if the storage cannot be read
     */
    public Applicant getApplicantByUsername(String username) throws Exception {
        return csvRepo.findByUsername(username);
    }

    /**
     * Finds an applicant by TA ID.
     *
     * @param id TA identifier
     * @return matching applicant, or null when not found
     * @throws Exception if the storage cannot be read
     */
    public Applicant getApplicantById(String id) throws Exception {
        return csvRepo.findById(id);
    }
}
