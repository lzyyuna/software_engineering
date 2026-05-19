package com.group4.tarecruitment.view;

import com.group4.tarecruitment.model.Job;
import com.group4.tarecruitment.service.MOService;
import com.group4.tarecruitment.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MOEditJobView {

    private final Stage stage;
    private final String moName;
    private final Job job;
    private final MOService moService = new MOService();

    public MOEditJobView(Stage stage, String moName, Job job) {
        this.stage = stage;
        this.moName = moName;
        this.job = job;
    }

    public Parent createContent() {
        Label title = new Label("Edit Position");
        title.getStyleClass().add("page-title");

        Label jobIdLabel = new Label("Position ID: " + job.getJobId());
        jobIdLabel.getStyleClass().add("muted-text");

        VBox header = new VBox(6, title, jobIdLabel);
        header.setMaxWidth(720);

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(180);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(col1, col2);

        TextField courseNameField = new TextField(job.getCourseName());

        ComboBox<String> positionTypeCombo = new ComboBox<>();
        positionTypeCombo.getItems().addAll("Module TA", "Invigilation TA");
        positionTypeCombo.setValue(job.getPositionType());
        positionTypeCombo.setMaxWidth(Double.MAX_VALUE);

        TextField workloadField = new TextField(String.valueOf(job.getWeeklyWorkload()));

        TextArea skillArea = new TextArea(job.getSkillRequirements() != null ? job.getSkillRequirements() : "");
        skillArea.setPrefRowCount(3);

        TextArea contentArea = new TextArea(job.getJobContent() != null ? job.getJobContent() : "");
        contentArea.setPrefRowCount(3);

        TextField deadlineField = new TextField(job.getDeadline() != null ? job.getDeadline() : "");
        deadlineField.setPromptText("YYYY-MM-DD");

        addFormRow(form, 0, "Course Name *",             courseNameField);
        addFormRow(form, 1, "Position Type *",           positionTypeCombo);
        addFormRow(form, 2, "Weekly Workload * (h/week)",workloadField);
        addFormRow(form, 3, "Skill Requirements",        skillArea);
        addFormRow(form, 4, "Job Content",               contentArea);
        addFormRow(form, 5, "Application Deadline",      deadlineField);

        VBox formCard = new VBox(12, form);
        formCard.getStyleClass().add("surface-card");
        formCard.setMaxWidth(720);

        Button saveBtn = new Button("Save Changes");
        Button cancelBtn = new Button("Cancel");
        saveBtn.getStyleClass().add("btn-success");
        cancelBtn.getStyleClass().add("btn-muted");

        HBox buttonBox = new HBox(12, saveBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");

        saveBtn.setOnAction(e -> {
            try {
                statusLabel.getStyleClass().removeAll("status-error", "status-success");

                String courseName = courseNameField.getText().trim();
                String positionType = positionTypeCombo.getValue();
                String workloadStr = workloadField.getText().trim();

                if (courseName.isEmpty()) { setError(statusLabel, "Course Name is required."); return; }
                if (positionType == null || positionType.isEmpty()) { setError(statusLabel, "Position Type is required."); return; }
                if (workloadStr.isEmpty()) { setError(statusLabel, "Weekly Workload is required."); return; }

                int workload;
                try {
                    workload = Integer.parseInt(workloadStr);
                    if (workload <= 0) { setError(statusLabel, "Workload must be a positive number."); return; }
                } catch (NumberFormatException ex) {
                    setError(statusLabel, "Workload must be a valid number.");
                    return;
                }

                boolean success = moService.editJob(
                        job.getJobId(), moName, courseName, positionType, workload,
                        skillArea.getText().trim(), contentArea.getText().trim(), deadlineField.getText().trim()
                );

                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Position updated successfully.");
                    alert.showAndWait();

                    MOViewJobsView viewJobsView = new MOViewJobsView(stage, moName);
                    stage.setScene(ThemeManager.createScene(viewJobsView.createContent(), 1000, 700));
                } else {
                    setError(statusLabel, "Failed to update. The position may no longer be in 'Recruiting' status.");
                }
            } catch (Exception ex) {
                setError(statusLabel, ex.getMessage());
                ex.printStackTrace();
            }
        });

        cancelBtn.setOnAction(e -> {
            MOViewJobsView viewJobsView = new MOViewJobsView(stage, moName);
            stage.setScene(ThemeManager.createScene(viewJobsView.createContent(), 1000, 700));
        });

        VBox root = new VBox(18, header, formCard, buttonBox, statusLabel);
        root.getStyleClass().add("app-page");
        root.setPadding(new Insets(28));
        root.setAlignment(Pos.TOP_CENTER);

        return root;
    }

    private void addFormRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("login-label");
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        if (field instanceof Control c) c.setMaxWidth(Double.MAX_VALUE);
    }

    private void setError(Label label, String text) {
        label.getStyleClass().removeAll("status-success");
        if (!label.getStyleClass().contains("status-error")) {
            label.getStyleClass().add("status-error");
        }
        label.setText(text);
    }
}
