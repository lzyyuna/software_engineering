package com.group4.tarecruitment.repository;

import com.group4.tarecruitment.model.Job;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists TA recruitment job records in CSV format.
 */
public class JobRepository {
    private final String filePath;

    /**
     * Creates a repository using the default jobs CSV file.
     */
    public JobRepository() {
        this.filePath = "data/jobs.csv";
    }

    /**
     * Creates a repository using a custom jobs CSV path.
     *
     * @param filePath path to the jobs CSV file
     */
    public JobRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Creates a repository using a custom jobs CSV path.
     *
     * @param path path to the jobs CSV file
     */
    public JobRepository(Path path) {
        this.filePath = path.toString();
    }

    /**
     * Loads every job from CSV storage.
     *
     * @return list of jobs
     * @throws Exception if the file cannot be read
     */
    public List<Job> loadAll() throws Exception {
        List<Job> jobs = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return jobs;

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            boolean firstLine = true;
            while ((line = reader.readNext()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (line.length < 11) continue;
                String dept = line.length >= 12 ? line[11] : "General/Others";
                Job job = new Job(
                        line[0], line[1], line[2], Integer.parseInt(line[3]),
                        line[4], line[5], line[6], line[7], line[8], line[9], line[10], dept
                );
                jobs.add(job);
            }
        }
        return jobs;
    }

    /**
     * Rewrites CSV storage with the provided jobs.
     *
     * @param jobs jobs to persist
     * @throws Exception if the file cannot be written
     */
    public void saveAll(List<Job> jobs) throws Exception {
        File dir = new File(filePath).getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeNext(new String[]{
                    "jobId", "courseName", "positionType", "weeklyWorkload",
                    "moName", "moEmail", "status", "releaseTime", "skillRequirements",
                    "jobContent", "deadline", "department"
            });
            for (Job j : jobs) {
                writer.writeNext(new String[]{
                        j.getJobId(), j.getCourseName(), j.getPositionType(),
                        String.valueOf(j.getWeeklyWorkload()), j.getMoName(),
                        j.getMoEmail(), j.getStatus(), j.getReleaseTime(), j.getSkillRequirements(),
                        j.getJobContent(), j.getDeadline(), j.getDepartment()
                });
            }
        }
    }
}
