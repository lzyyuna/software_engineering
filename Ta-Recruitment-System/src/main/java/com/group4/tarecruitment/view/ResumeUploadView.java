package com.group4.tarecruitment.view;

import com.group4.tarecruitment.controller.ProfileController;
import com.group4.tarecruitment.model.Applicant;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;

/**
 * Builds the TA resume management page for upload, replacement, and preview.
 */
public class ResumeUploadView {
    private final Applicant applicant;
    private final Stage stage;
    private final ProfileController controller = new ProfileController();

    public ResumeUploadView(Applicant applicant, Stage stage) {
        this.applicant = applicant;
        this.stage = stage;
    }

    public Parent createContent() {
        Label title = new Label("Upload Resume");
        title.getStyleClass().add("page-title");

        Label subTitle = new Label("Supported formats: txt / pdf / doc / docx · maximum size 10 MB.");
        subTitle.getStyleClass().add("page-subtitle");

        VBox header = new VBox(6, title, subTitle);
        header.setAlignment(Pos.CENTER);

        Button uploadBtn = new Button("Upload Resume");
        Button replaceBtn = new Button("Replace Resume");
        Button viewBtn = new Button("View Resume");

        uploadBtn.getStyleClass().add("btn-primary");
        replaceBtn.getStyleClass().add("btn-info");
        viewBtn.getStyleClass().add("btn-success");

        for (Button b : new Button[]{uploadBtn, replaceBtn, viewBtn}) {
            b.setPrefWidth(160);
        }

        HBox btnBox = new HBox(16, uploadBtn, replaceBtn, viewBtn);
        btnBox.setAlignment(Pos.CENTER);

        Label statusLabel = new Label();
        Label pathLabel = new Label();
        statusLabel.getStyleClass().add("section-title");
        pathLabel.getStyleClass().add("muted-text");

        if (applicant.getResumePath() != null && !applicant.getResumePath().isBlank()) {
            statusLabel.setText("Status: Uploaded");
            statusLabel.getStyleClass().add("status-success");
            pathLabel.setText("File: " + new File(applicant.getResumePath()).getName());
        } else {
            statusLabel.setText("Status: Not Uploaded");
            pathLabel.setText("File: None");
        }

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(300);

        VBox statusBox = new VBox(12, statusLabel, pathLabel, progressBar);
        statusBox.getStyleClass().add("surface-card");
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setMaxWidth(560);

        uploadBtn.setOnAction(e -> controller.uploadResume(applicant, statusLabel, pathLabel, progressBar));
        replaceBtn.setOnAction(e -> controller.uploadResume(applicant, statusLabel, pathLabel, progressBar));
        viewBtn.setOnAction(e -> controller.viewResume(applicant, statusLabel));

        Button backToHomeBtn = new Button("Back to TA Home");
        backToHomeBtn.getStyleClass().add("btn-muted");
        backToHomeBtn.setOnAction(e -> {
            TAHomeView taHomeView = new TAHomeView(stage, applicant);
            stage.getScene().setRoot(taHomeView.createContent());
            stage.setTitle("TA Dashboard");
        });

        HBox backBtnBox = new HBox(backToHomeBtn);
        backBtnBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, header, btnBox, statusBox, backBtnBox);
        root.getStyleClass().add("app-page");
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);

        return root;
    }
}
