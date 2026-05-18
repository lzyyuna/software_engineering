package com.group4.tarecruitment.repository;

import com.group4.tarecruitment.model.Applicant;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists TA applicant profiles in CSV format.
 */
public class ApplicantCsvRepository {
    private final String filePath;

    /**
     * Creates a repository using the default applicant CSV file.
     */
    public ApplicantCsvRepository() {
        this.filePath = "data/applicants.csv";
    }

    /**
     * Creates a repository using a custom CSV path.
     *
     * @param path path to the applicant CSV file
     */
    public ApplicantCsvRepository(Path path) {
        this.filePath = path.toString();
    }

    /**
     * Appends an applicant to CSV storage.
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
     * Loads every applicant from CSV storage.
     *
     * @return list of applicants
     * @throws Exception if the file cannot be read
     */
    public List<Applicant> loadAll() throws Exception {
        List<Applicant> applicants = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return applicants;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean firstLine = true;
            while ((line = reader.readNext()) != null) {
                if (firstLine) { firstLine = false; continue; }
                Applicant applicant = new Applicant();
                applicant.setTaId(line[0]);
                applicant.setStudentId(line[1]);
                applicant.setName(line[2]);
                applicant.setEmail(line[3]);
                applicant.setCourses(line[4]);
                applicant.setSkillTags(line[5]);
                applicant.setContact(line[6]);
                if (line.length > 7) applicant.setPassword(line[7]);
                if (line.length > 8) applicant.setUsername(line[8]);
                if (line.length > 9) applicant.setResumePath(line[9]);
                applicants.add(applicant);
            }
        }
        return applicants;
    }

    /**
     * Rewrites CSV storage with the provided applicants.
     *
     * @param applicants applicants to persist
     * @throws Exception if the file cannot be written
     */
    public void saveAll(List<Applicant> applicants) throws Exception {
        File dir = new File(filePath).getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeNext(new String[]{
                    "taId", "studentId", "name", "email",
                    "courses", "skillTags", "contact", "password",
                    "username", "resumePath"
            });
            for (Applicant a : applicants) {
                writer.writeNext(new String[]{
                        a.getTaId(), a.getStudentId(), a.getName(), a.getEmail(),
                        a.getCourses(), a.getSkillTags(), a.getContact(),
                        a.getPassword()    == null ? "" : a.getPassword(),
                        a.getUsername()    == null ? "" : a.getUsername(),
                        a.getResumePath()  == null ? "" : a.getResumePath()
                });
            }
        }
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

    /**
     * Finds an applicant by student ID.
     *
     * @param studentId student identifier
     * @return matching applicant, or null when not found
     * @throws Exception if the file cannot be read
     */
    public Applicant findByStudentId(String studentId) throws Exception {
        for (Applicant a : loadAll()) {
            if (studentId.equals(a.getStudentId())) return a;
        }
        return null;
    }

    /**
     * Finds an applicant by TA ID.
     *
     * @param taId TA identifier
     * @return matching applicant, or null when not found
     * @throws Exception if the file cannot be read
     */
    public Applicant findById(String taId) throws Exception {
        for (Applicant a : loadAll()) {
            if (taId.equals(a.getTaId())) return a;
        }
        return null;
    }
}
