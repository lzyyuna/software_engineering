package com.group4.tarecruitment.view;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Builds the main dashboard for logged-in TA users.
 */
public class TAHomeView {

    private final Stage stage;
    private final Applicant applicant;

    public TAHomeView(Stage stage, Applicant applicant) {
        this.stage = stage;
        this.applicant = applicant;
    }

    /**
     * Creates the TA dashboard page with navigation to profile, resume, and job list pages.
     *
     * @return dashboard root node
     */
    public Parent createContent() {
        Label title = new Label("TA Dashboard");
        title.getStyleClass().add("page-title");

        Label welcomeLabel = new Label("Welcome, " + applicant.getUsername());
        welcomeLabel.getStyleClass().add("dashboard-welcome");

        Button profileBtn = new Button("Profile Details");
        Button resumeBtn = new Button("Upload Resume");
        Button jobListBtn = new Button("View Available Jobs");
        Button backBtn = new Button("Back to Role Selection");

        profileBtn.getStyleClass().add("primary-button");
        resumeBtn.getStyleClass().add("secondary-button");
        jobListBtn.getStyleClass().add("primary-button");
        backBtn.getStyleClass().add("neutral-button");

        profileBtn.setPrefWidth(260);
        resumeBtn.setPrefWidth(260);
        jobListBtn.setPrefWidth(260);
        backBtn.setPrefWidth(260);

        profileBtn.setOnAction(e -> {
            ProfileDetailView profileView = new ProfileDetailView(applicant, stage);
            stage.getScene().setRoot(profileView.getView());
            stage.setTitle("Profile Details & Resume Upload");
        });

        resumeBtn.setOnAction(e -> {
            ResumeUploadView resumeView = new ResumeUploadView(applicant, stage);
            stage.getScene().setRoot(resumeView.createContent());
            stage.setTitle("Resume Upload");
        });

        jobListBtn.setOnAction(e -> {
            JobListView jobListView = new JobListView(stage, applicant);
            stage.getScene().setRoot(jobListView.createContent());
            stage.setTitle("Available TA Positions");
        });

        backBtn.setOnAction(e -> {
            RoleSelectView roleSelectView = new RoleSelectView(stage);
            stage.setScene(ThemeManager.createScene(roleSelectView.createContent(), 1000, 700));
            stage.setTitle("TA Recruitment System");
        });

        VBox hero = new VBox(8, title, welcomeLabel);
        hero.getStyleClass().add("dashboard-hero");
        hero.setMaxWidth(560);

        VBox card = new VBox(14, profileBtn, resumeBtn, jobListBtn, backBtn);
        card.getStyleClass().add("dashboard-card");
        card.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, hero, card);
        root.getStyleClass().add("dashboard-page");
        root.setPadding(new Insets(36));
        root.setAlignment(Pos.TOP_CENTER);

        return root;
    }
}
