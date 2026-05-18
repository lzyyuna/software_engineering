package com.group4.tarecruitment.controller;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.service.ApplicantService;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Controls TA resume upload, replacement, and local resume preview actions.
 */
public class ProfileController {
    private final ApplicantService service = new ApplicantService();
    private final String RESUME_DIR = "data/resumes/";

    /**
     * Opens a file chooser, copies the selected resume into the application data
     * directory, and saves the relative resume path on the applicant profile.
     *
     * @param applicant applicant whose resume is being uploaded
     * @param status label used to display upload status
     * @param pathLabel label used to display the uploaded file name
     * @param pb progress bar used during upload
     */
    public void uploadResume(Applicant applicant, Label status, Label pathLabel, ProgressBar pb) {
        new File(RESUME_DIR).mkdirs();

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Document Files", "*.txt", "*.pdf", "*.doc", "*.docx")
        );

        File file = chooser.showOpenDialog(null);
        if (file == null) return;

        if (file.length() > 10 * 1024 * 1024) {
            status.setText("Error: file exceeds the maximum size of 10MB.");
            status.setStyle("-fx-text-fill:red;");
            return;
        }

        pb.setVisible(true);
        status.setText("Uploading...");
        pb.setProgress(0.3);

        new Thread(() -> {
            try {
                String newName = applicant.getStudentId() + "_" + file.getName();
                File target = new File(RESUME_DIR + newName);
                Files.copy(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

                applicant.setResumePath(RESUME_DIR + newName);
                service.updateApplicant(applicant);

                Platform.runLater(() -> {
                    pb.setProgress(1.0);
                    status.setText("Upload successful");
                    status.setStyle("-fx-text-fill:green;");
                    pathLabel.setText("File: " + target.getName());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    status.setText("Upload failed");
                    status.setStyle("-fx-text-fill:red;");
                });
            }
        }).start();
    }

    /**
     * Opens the applicant's uploaded resume using the operating system default
     * desktop application.
     *
     * @param applicant applicant whose resume should be opened
     * @param statusLabel label used to display the open result
     */
    public void viewResume(Applicant applicant, Label statusLabel) {
        try {
            if (applicant == null || applicant.getResumePath() == null || applicant.getResumePath().isBlank()) {
                statusLabel.setText("Error: no resume uploaded yet");
                statusLabel.setStyle("-fx-text-fill:red;");
                return;
            }

            File file = new File(applicant.getResumePath());

            if (!file.exists()) {
                statusLabel.setText("Error: resume file not found");
                statusLabel.setStyle("-fx-text-fill:red;");
                return;
            }

            if (!Desktop.isDesktopSupported()) {
                statusLabel.setText("Error: desktop open is not supported on this system");
                statusLabel.setStyle("-fx-text-fill:red;");
                return;
            }

            Desktop.getDesktop().open(file);

            statusLabel.setText("Resume opened");
            statusLabel.setStyle("-fx-text-fill:green;");
        } catch (Exception e) {
            statusLabel.setText("Error: failed to open resume");
            statusLabel.setStyle("-fx-text-fill:red;");
            e.printStackTrace();
        }
    }
}
