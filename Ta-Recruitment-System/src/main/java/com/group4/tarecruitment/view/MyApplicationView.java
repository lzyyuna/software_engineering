package com.group4.tarecruitment.view;

import com.group4.tarecruitment.model.Application;
import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.model.Job;
import com.group4.tarecruitment.service.JobService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

/**
 * Builds the TA application history page.
 */
public class MyApplicationView {
    private final Stage stage;
    private final Applicant applicant;
    private final JobService jobService = new JobService();

    public MyApplicationView(Stage stage, Applicant applicant) {
        this.stage = stage;
        this.applicant = applicant;
    }

    /**
     * Creates the application history page.
     *
     * @return application history root node
     */
    public Parent createContent() {
        Label title = new Label("My Applications");
        title.setFont(new Font(18));
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button refreshBtn = new Button("Refresh Status");
        Button backToListBtn = new Button("Back to Job List");
        Button backToHomeBtn = new Button("Back to TA Home");

        String btnStyle = "-fx-font-size: 14px; -fx-padding: 7 14; -fx-background-radius: 5; -fx-font-weight: bold;";
        refreshBtn.setStyle(btnStyle + "-fx-background-color: #3498db; -fx-text-fill: white;");
        backToListBtn.setStyle(btnStyle + "-fx-background-color: #95a5a6; -fx-text-fill: white;");
        backToHomeBtn.setStyle(btnStyle + "-fx-background-color: #2ecc71; -fx-text-fill: white;");

        HBox topBar = new HBox(10, refreshBtn, backToListBtn, backToHomeBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox appListBox = new VBox(10);
        appListBox.setPadding(new Insets(15));
        appListBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        loadApplications(appListBox);

        refreshBtn.setOnAction(e -> loadApplications(appListBox));

        backToListBtn.setOnAction(e -> {
            JobListView jobListView = new JobListView(stage, applicant);
            stage.getScene().setRoot(jobListView.createContent());
            stage.setTitle("Available TA Positions");
        });

        backToHomeBtn.setOnAction(e -> {
            TAHomeView taHomeView = new TAHomeView(stage, applicant);
            stage.getScene().setRoot(taHomeView.createContent());
            stage.setTitle("TA Dashboard");
        });

        VBox root = new VBox(15, title, topBar, appListBox);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f5f6fa;");
        return root;
    }

    private void loadApplications(VBox appListBox) {
        appListBox.getChildren().clear();
        try {
            List<Application> apps = jobService.getMyApplications(applicant.getTaId());
            if (apps.isEmpty()) {
                Label emptyLabel = new Label("No application records found.");
                emptyLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #7f8c8d;");
                appListBox.getChildren().add(emptyLabel);
                return;
            }

            for (Application app : apps) {
                Job job = jobService.getJobById(app.getJobId());

                HBox appItem = new HBox(15);
                appItem.setAlignment(Pos.CENTER_LEFT);
                appItem.setStyle("-fx-padding: 12; -fx-background-color: white; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4,0,0,1);");

                Label courseLabel = new Label("Course: " + (job != null ? job.getCourseName() : "Unknown Position"));
                Label timeLabel = new Label("Application Time: " + app.getApplicationTime());
                Label statusLabel = new Label("Status: " + app.getStatus());

                if ("Pending".equalsIgnoreCase(app.getStatus())) {
                    statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                } else if ("Approved".equalsIgnoreCase(app.getStatus())) {
                    statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                } else if ("Rejected".equalsIgnoreCase(app.getStatus())) {
                    statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }

                Button detailBtn = new Button("View Details");
                detailBtn.setStyle("-fx-font-size: 13px; -fx-padding: 6 12; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");

                detailBtn.setOnAction(e -> showAppDetail(app, job));
                appItem.getChildren().addAll(courseLabel, timeLabel, statusLabel, detailBtn);
                appListBox.getChildren().add(appItem);
            }
        } catch (Exception e) {
            appListBox.getChildren().add(new Label("Load failed: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private void showAppDetail(Application app, Job job) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Application Details");
        alert.setHeaderText(null);

        String reviewComment = app.getReviewComment() == null || app.getReviewComment().isEmpty()
                ? "None"
                : app.getReviewComment();

        String content = String.format(
                "Application ID: %s\nPosition: %s\nApplication Time: %s\nStatus: %s\nReview Comment: %s",
                app.getApplicationId(),
                job != null ? job.getCourseName() : "Unknown Position",
                app.getApplicationTime(),
                app.getStatus(),
                reviewComment
        );

        alert.setContentText(content);
        alert.showAndWait();
    }
}
