package com.group4.tarecruitment.repository;

import com.group4.tarecruitment.model.Application;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists TA job application records in CSV format.
 */
public class ApplicationRepository {
    private final String filePath;

    /**
     * Creates a repository using the default applications CSV file.
     */
    public ApplicationRepository() {
        this.filePath = "data/applications.csv";
    }

    /**
     * Creates a repository using a custom applications CSV path.
     *
     * @param path path to the applications CSV file
     */
    public ApplicationRepository(Path path) {
        this.filePath = path.toString();
    }

    /**
     * Appends an application record to CSV storage.
     *
     * @param app application to save
     * @throws Exception if the file cannot be read or written
     */
    public void save(Application app) throws Exception {
        List<Application> apps = loadAll();
        apps.add(app);
        saveAll(apps);
    }

    /**
     * Loads every application from CSV storage.
     *
     * @return list of application records
     * @throws Exception if the file cannot be read
     */
    public List<Application> loadAll() throws Exception {
        List<Application> apps = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return apps;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean firstLine = true;
            while ((line = reader.readNext()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (line.length < 6) continue;
                apps.add(new Application(
                        line[0], line[1], line[2], line[3], line[4], line[5]
                ));
            }
        }
        return apps;
    }

    /**
     * Rewrites CSV storage with the provided applications.
     *
     * @param apps applications to persist
     * @throws Exception if the file cannot be written
     */
    public void saveAll(List<Application> apps) throws Exception {
        File dir = new File(filePath).getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeNext(new String[]{
                    "applicationId", "taId", "jobId", "applicationTime", "status", "reviewComment"
            });
            for (Application a : apps) {
                writer.writeNext(new String[]{
                        a.getApplicationId(), a.getTaId(), a.getJobId(),
                        a.getApplicationTime(), a.getStatus(), a.getReviewComment()
                });
            }
        }
    }
}
