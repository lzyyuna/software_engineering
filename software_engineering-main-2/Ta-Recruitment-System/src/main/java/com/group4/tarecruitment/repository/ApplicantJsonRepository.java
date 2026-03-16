package com.group4.tarecruitment.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.group4.tarecruitment.model.Applicant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApplicantJsonRepository {

    private static final String FILE_PATH = "data/applicants.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void save(Applicant applicant) throws Exception {
        List<Applicant> applicants = loadAll();
        applicants.add(applicant);
        saveAll(applicants);
    }

    public List<Applicant> loadAll() throws Exception {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();

        return objectMapper.readValue(file,
                TypeFactory.defaultInstance().constructCollectionType(List.class, Applicant.class));
    }

    public void saveAll(List<Applicant> applicants) throws Exception {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdir();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), applicants);
    }
}