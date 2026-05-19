package com.group4.tarecruitment.view;

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

public class MOPostJobView {

    private final Stage stage;
    private final String moName;
    private final String moEmail;
    private final MOService moService = new MOService();

    public MOPostJobView(Stage stage, String moName, String moEmail) {
        this.stage = stage;
        this.moName = moName;
        this.moEmail = moEmail;
    }

    public Parent createContent() {
        Label title = new Label("Post TA Recruitment Position");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Fields marked with * are required.");
        subtitle.getStyleClass().add("page-subtitle");

        VBox header = new VBox(6, title, subtitle);
        header.setMaxWidth(720);

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(14);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(180);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(col1, col2);

        TextField courseNameField = new TextField();
        courseNameField.setPromptText("Enter course name");

        ComboBox<String> positionTypeCombo = new ComboBox<>();
        positionTypeCombo.getItems().addAll("Module TA", "Invigilation TA");
        positionTypeCombo.setPromptText("Select position type");
        positionTypeCombo.setMaxWidth(Double.MAX_VALUE);

        TextField workloadField = new TextField();
        workloadField.setPromptText("Enter weekly workload");

        TextField moNameField = new TextField(moName);
        moNameField.setEditable(false);

        TextField moEmailField = new TextField(moEmail);
        moEmailField.setEditable(false);

        TextArea skillArea = new TextArea();
        skillArea.setPromptText("Enter skill requirements");
        skillArea.setPrefRowCount(3);

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter job content / description");
        contentArea.setPrefRowCount(3);

        TextField deadlineField = new TextField();
        deadlineField.setPromptText("YYYY-MM-DD");

        addFormRow(form, 0, "Course Name *",                courseNameField);
        addFormRow(form, 1, "Position Type *",              positionTypeCombo);
        addFormRow(form, 2, "Weekly Workload * (h/week)",   workloadField);
        addFormRow(form, 3, "MO Name *",                    moNameField);
        addFormRow(form, 4, "MO Email *",                   moEmailField);
        addFormRow(form, 5, "Skill Requirements",           skillArea);
        addFormRow(form, 6, "Job Content",                  contentArea);
        addFormRow(form, 7, "Application Deadline",         deadlineField);

        VBox formCard = new VBox(12, form);
        formCard.getStyleClass().add("surface-card");
        formCard.setMaxWidth(720);

        Button postBtn = new Button("Post Position");
        Button backBtn = new Button("Back");
        postBtn.getStyleClass().add("btn-success");
        backBtn.getStyleClass().add("btn-muted");

        HBox buttonBox = new HBox(12, postBtn, backBtn);
        buttonBox.setAlignment(Pos.CENTER);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");

        postBtn.setOnAction(e -> {
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

                String jobId = moService.postJob(
                        courseName, positionType, workload, moName, moEmail,
                        skillArea.getText().trim(), contentArea.getText().trim(), deadlineField.getText().trim()
                );

                if (jobId != null) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Position posted successfully.\nJob ID: " + jobId);
                    alert.showAndWait();

                    courseNameField.clear();
                    positionTypeCombo.setValue(null);
                    workloadField.clear();
                    skillArea.clear();
                    contentArea.clear();
                    deadlineField.clear();
                    statusLabel.setText("");
                } else {
                    setError(statusLabel, "Failed to post position. Please check your input.");
                }
            } catch (Exception ex) {
                setError(statusLabel, ex.getMessage());
                ex.printStackTrace();
            }
        });

        backBtn.setOnAction(e -> {
            TeacherView teacherView = new TeacherView(stage, moName);
            stage.setScene(ThemeManager.createScene(teacherView.createContent(), 1000, 700));
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
        if (field instanceof javafx.scene.control.Control c) {
            c.setMaxWidth(Double.MAX_VALUE);
        }
    }

    private void setError(Label label, String text) {
        label.getStyleClass().removeAll("status-success");
        if (!label.getStyleClass().contains("status-error")) {
            label.getStyleClass().add("status-error");
        }
        label.setText(text);
    }
}
