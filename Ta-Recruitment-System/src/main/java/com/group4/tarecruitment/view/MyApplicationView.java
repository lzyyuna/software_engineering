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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

    public Parent createContent() {
        Label title = new Label("My Applications");
        title.getStyleClass().add("page-title");

        Button refreshBtn = new Button("Refresh Status");
        Button backToListBtn = new Button("Back to Job List");
        Button backToHomeBtn = new Button("Back to TA Home");

        refreshBtn.getStyleClass().add("btn-primary");
        backToListBtn.getStyleClass().add("btn-muted");
        backToHomeBtn.getStyleClass().add("btn-success");

        HBox topBar = new HBox(10, refreshBtn, backToListBtn, backToHomeBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("toolbar");

        VBox appListBox = new VBox(12);
        appListBox.getStyleClass().add("list-container");

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

        VBox root = new VBox(16, title, topBar, appListBox);
        root.getStyleClass().add("app-page");
        root.setPadding(new Insets(28));
        return root;
    }

    private void loadApplications(VBox appListBox) {
        appListBox.getChildren().clear();
        try {
            List<Application> apps = jobService.getMyApplications(applicant.getTaId());
            if (apps.isEmpty()) {
                Label emptyLabel = new Label("No application records found.");
                emptyLabel.getStyleClass().add("empty-text");
                appListBox.getChildren().add(emptyLabel);
                return;
            }

            for (Application app : apps) {
                Job job = jobService.getJobById(app.getJobId());

                Label courseLabel = new Label((job != null ? job.getCourseName() : "Unknown Position"));
                courseLabel.getStyleClass().add("section-title");

                Label timeLabel = new Label("Applied: " + app.getApplicationTime());
                timeLabel.getStyleClass().add("muted-text");

                Label statusLabel = new Label(app.getStatus());
                statusLabel.getStyleClass().addAll("badge", statusBadgeClass(app.getStatus()));

                Button detailBtn = new Button("View Details");
                detailBtn.getStyleClass().add("btn-info");
                detailBtn.setOnAction(e -> showAppDetail(app, job));

                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox row = new HBox(14, courseLabel, timeLabel, spacer, statusLabel, detailBtn);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("list-item-card");

                appListBox.getChildren().add(row);
            }
        } catch (Exception e) {
            Label err = new Label("Load failed: " + e.getMessage());
            err.getStyleClass().add("status-error");
            appListBox.getChildren().add(err);
            e.printStackTrace();
        }
    }

    private String statusBadgeClass(String status) {
        if ("Approved".equalsIgnoreCase(status)) return "badge-success";
        if ("Rejected".equalsIgnoreCase(status)) return "badge-danger";
        if ("Pending".equalsIgnoreCase(status))  return "badge-warning";
        return "badge-neutral";
    }

    private void showAppDetail(Application app, Job job) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Application Details");
        alert.setHeaderText(null);

        String reviewComment = app.getReviewComment() == null || app.getReviewComment().isEmpty()
                ? "None"
                : app.getReviewComment();

        String content = String.format(
                "Application ID: %s%nPosition: %s%nApplication Time: %s%nStatus: %s%nReview Comment: %s",
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
