package com.group4.tarecruitment.view;

import com.group4.tarecruitment.model.Job;
import com.group4.tarecruitment.service.MOService;
import com.group4.tarecruitment.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class MOViewJobsView {

    private final Stage stage;
    private final String moName;
    private final MOService moService = new MOService();
    private VBox jobListBox;

    public MOViewJobsView(Stage stage, String moName) {
        this.stage = stage;
        this.moName = moName;
    }

    public Parent createContent() {
        Label title = new Label("My Posted Positions");
        title.getStyleClass().add("page-title");

        Button refreshBtn = new Button("Refresh List");
        Button backBtn = new Button("Back to Home");
        refreshBtn.getStyleClass().add("btn-primary");
        backBtn.getStyleClass().add("btn-muted");

        HBox topBar = new HBox(10, refreshBtn, backBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("toolbar");

        jobListBox = new VBox(12);
        jobListBox.getStyleClass().add("list-container");

        loadJobs();

        refreshBtn.setOnAction(e -> loadJobs());

        backBtn.setOnAction(e -> {
            TeacherView teacherView = new TeacherView(stage, moName);
            stage.setScene(ThemeManager.createScene(teacherView.createContent(), 1000, 700));
        });

        VBox root = new VBox(16, title, topBar, jobListBox);
        root.getStyleClass().add("app-page");
        root.setPadding(new Insets(28));

        return root;
    }

    private void loadJobs() {
        jobListBox.getChildren().clear();
        try {
            List<Job> jobs = moService.getMyPostedJobs(moName);

            if (jobs.isEmpty()) {
                Label emptyLabel = new Label("No Posted Positions");
                emptyLabel.getStyleClass().add("empty-text");
                jobListBox.getChildren().add(emptyLabel);
                return;
            }

            for (Job job : jobs) {
                VBox jobItem = new VBox(8);
                jobItem.getStyleClass().add("list-item-card");

                Label courseLabel = new Label(job.getCourseName() == null ? "Untitled Course" : job.getCourseName());
                courseLabel.getStyleClass().add("section-title");

                Label basicInfoLabel = new Label(
                        "ID: " + safe(job.getJobId())
                                + "   ·   Type: " + safe(job.getPositionType())
                                + "   ·   Weekly Workload: " + job.getWeeklyWorkload() + "h"
                                + "   ·   Released: " + safe(job.getReleaseTime())
                );
                basicInfoLabel.getStyleClass().add("muted-text");

                Label statusLabel = new Label(safe(job.getStatus()));
                statusLabel.getStyleClass().addAll("badge", statusBadgeClass(job.getStatus()));

                Button detailBtn = new Button("View Details");
                Button editBtn = new Button("Edit");
                Button closeBtn = new Button("Close Position");

                detailBtn.getStyleClass().add("btn-info");
                editBtn.getStyleClass().add("btn-warning");
                closeBtn.getStyleClass().add("btn-danger");

                if (!"Recruiting".equals(job.getStatus())) {
                    editBtn.setDisable(true);
                    closeBtn.setDisable(true);
                }

                detailBtn.setOnAction(e -> showJobDetail(job));
                editBtn.setOnAction(e -> {
                    MOEditJobView editView = new MOEditJobView(stage, moName, job);
                    stage.setScene(ThemeManager.createScene(editView.createContent(), 1000, 700));
                });
                closeBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Close");
                    confirm.setHeaderText("Close Position");
                    confirm.setContentText("Are you sure you want to close this position?\n" +
                            "Position ID: " + job.getJobId() + "\n" +
                            "Course: " + job.getCourseName());

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                boolean success = moService.closeJob(job.getJobId(), moName);
                                if (success) {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Success");
                                    alert.setContentText("Position closed successfully.");
                                    alert.showAndWait();
                                    loadJobs();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                });

                HBox bottomBar = new HBox(10);
                bottomBar.setAlignment(Pos.CENTER_LEFT);
                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                bottomBar.getChildren().addAll(statusLabel, spacer, detailBtn, editBtn, closeBtn);

                jobItem.getChildren().addAll(courseLabel, basicInfoLabel, bottomBar);
                jobListBox.getChildren().add(jobItem);
            }
        } catch (Exception e) {
            Label err = new Label("Load failed: " + e.getMessage());
            err.getStyleClass().add("status-error");
            jobListBox.getChildren().add(err);
            e.printStackTrace();
        }
    }

    private String safe(String value) { return value == null ? "" : value; }

    private String statusBadgeClass(String status) {
        if ("Recruiting".equals(status)) return "badge-success";
        if ("Closed".equals(status))     return "badge-danger";
        return "badge-neutral";
    }

    private void showJobDetail(Job job) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Position Detail");
        dialog.setHeaderText(null);

        Label titleLabel = new Label(safe(job.getCourseName()));
        titleLabel.getStyleClass().add("page-title");

        Label idLabel = new Label("ID: " + safe(job.getJobId()));
        idLabel.getStyleClass().add("muted-text");

        Label statusBadge = new Label(safe(job.getStatus()));
        statusBadge.getStyleClass().addAll("badge", statusBadgeClass(job.getStatus()));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox titleBox = new VBox(4, titleLabel, idLabel);
        HBox headerBox = new HBox(12, titleBox, spacer, statusBadge);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getStyleClass().add("hero-card");

        Label sectionTitle = new Label("Position Information");
        sectionTitle.getStyleClass().add("section-title");

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(10);
        addInfoRow(infoGrid, 0, "Position Type:",   safe(job.getPositionType()));
        addInfoRow(infoGrid, 1, "Weekly Workload:", job.getWeeklyWorkload() + " hours/week");
        addInfoRow(infoGrid, 2, "MO Name:",         safe(job.getMoName()));
        addInfoRow(infoGrid, 3, "MO Email:",        safe(job.getMoEmail()));
        addInfoRow(infoGrid, 4, "Release Time:",    safe(job.getReleaseTime()));
        addInfoRow(infoGrid, 5, "Deadline:",        job.getDeadline() != null ? job.getDeadline() : "Not specified");

        VBox infoSection = new VBox(10, sectionTitle, new Separator(), infoGrid);
        infoSection.getStyleClass().add("surface-card");

        Label reqTitle = new Label("Requirements & Content");
        reqTitle.getStyleClass().add("section-title");

        VBox skillBox = createInfoBox("Skill Requirements:", job.getSkillRequirements());
        VBox contentBox = createInfoBox("Job Content:", job.getJobContent());
        VBox reqSection = new VBox(10, reqTitle, new Separator(), skillBox, contentBox);
        reqSection.getStyleClass().add("surface-card");

        VBox content = new VBox(16, headerBox, infoSection, reqSection);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("app-page");

        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(560);
        dialog.getDialogPane().setPrefHeight(600);
        java.net.URL css = getClass().getResource("/styles/app-theme.css");
        if (css != null) dialog.getDialogPane().getStylesheets().add(css.toExternalForm());

        dialog.showAndWait();
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("profile-info-label");
        Label valueNode = new Label(value != null ? value : "N/A");
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private VBox createInfoBox(String title, String content) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("profile-info-label");
        Label contentLabel = new Label(content != null && !content.isEmpty() ? content : "Not specified");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(480);
        return new VBox(6, titleLabel, contentLabel);
    }
}
