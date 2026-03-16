package com.group4.tarecruitment.repository;

import com.group4.tarecruitment.model.Applicant;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ApplicantCsvRepository {

    private static final String FILE_PATH = "data/applicants.csv";

    public void save(Applicant applicant) throws Exception {
        List<Applicant> applicants = loadAll();
        applicants.add(applicant);
        saveAll(applicants);
    }

    public List<Applicant> loadAll() throws Exception {
        List<Applicant> applicants = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) return applicants;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean firstLine = true;

            while ((line = reader.readNext()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                applicants.add(new Applicant(
                        line[0],
                        line[1],
                        line[2],
                        line[3]
                ));
            }
        }
        return applicants;
    }

    public void saveAll(List<Applicant> applicants) throws Exception {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdir();

        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
            writer.writeNext(new String[]{"id", "name", "email", "skills"});
            for (Applicant a : applicants) {
                writer.writeNext(new String[]{
                        a.getId(),
                        a.getName(),
                        a.getEmail(),
                        a.getSkills()
                });
            }
        }
    }
}