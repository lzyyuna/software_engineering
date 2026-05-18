package com.group4.tarecruitment.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.tarecruitment.model.Applicant;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists TA applicant profiles in JSON format.
 */
public class ApplicantJsonRepository {
    private final String filePath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a repository using the default applicant JSON file.
     */
    public ApplicantJsonRepository() {
        this.filePath = "data/applicants.json";
    }

    /**
     * Creates a repository using a custom JSON path.
     *
     * @param path path to the applicant JSON file
     */
    public ApplicantJsonRepository(Path path) {
        this.filePath = path.toString();
    }

    /**
     * Appends an applicant to JSON storage.
     *
     * @param applicant applicant to save
     * @throws Exception if the file cannot be read or written
     */
    public void save(Applicant applicant) throws Exception {
        List<Applicant> applicants = loadAll();
        applicants.add(applicant);
        saveAll(applicants);
    }

    /**
     * Loads every applicant from JSON storage.
     *
     * @return list of applicants
     * @throws Exception if the file cannot be read
     */
    public List<Applicant> loadAll() throws Exception {
        File file = new File(filePath);
        if (!file.exists()) return new ArrayList<>();
        return objectMapper.readValue(file, new TypeReference<List<Applicant>>() {});
    }

    /**
     * Rewrites JSON storage with the provided applicants.
     *
     * @param applicants applicants to persist
     * @throws Exception if the file cannot be written
     */
    public void saveAll(List<Applicant> applicants) throws Exception {
        File dir = new File(filePath).getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), applicants);
    }

    /**
     * Updates an applicant matched by TA ID.
     *
     * @param updatedApplicant applicant containing new values
     * @throws Exception if the file cannot be read or written
     */
    public void update(Applicant updatedApplicant) throws Exception {
        List<Applicant> applicants = loadAll();
        for (int i = 0; i < applicants.size(); i++) {
            if (applicants.get(i).getTaId().equals(updatedApplicant.getTaId())) {
                applicants.set(i, updatedApplicant);
                saveAll(applicants);
                return;
            }
        }
    }

    /**
     * Finds an applicant by login username.
     *
     * @param username login account username
     * @return matching applicant, or null when not found
     * @throws Exception if the file cannot be read
     */
    public Applicant findByUsername(String username) throws Exception {
        for (Applicant a : loadAll()) {
            if (username.equals(a.getUsername())) return a;
        }
        return null;
    }
}
