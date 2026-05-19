package com.group4.tarecruitment.view;

import com.group4.tarecruitment.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TeacherView {

    private final Stage stage;
    private final String moUsername;

    public TeacherView(Stage stage, String moUsername) {
        this.stage = stage;
        this.moUsername = moUsername;
    }

    public Parent createContent() {
        Label title = new Label("Teacher Dashboard");
        title.getStyleClass().add("page-title");

        Label welcomeLabel = new Label("Welcome, " + moUsername);
        welcomeLabel.getStyleClass().add("dashboard-welcome");

        VBox hero = new VBox(6, title, welcomeLabel);
        hero.getStyleClass().add("hero-card");
        hero.setMaxWidth(560);

        Button publishBtn = new Button("Post TA Position");
        Button viewJobsBtn = new Button("View My Posted Positions");
        Button reviewBtn = new Button("Review Applications");
        Button backBtn = new Button("Back to Role Selection");

        publishBtn.getStyleClass().add("btn-primary");
        viewJobsBtn.getStyleClass().add("btn-info");
        reviewBtn.getStyleClass().add("btn-success");
        backBtn.getStyleClass().add("btn-muted");

        for (Button b : new Button[]{publishBtn, viewJobsBtn, reviewBtn, backBtn}) {
            b.setPrefWidth(280);
        }

        publishBtn.setOnAction(e -> {
            MOPostJobView postJobView = new MOPostJobView(stage, moUsername, moUsername + "@bupt.edu");
            stage.setScene(ThemeManager.createScene(postJobView.createContent(), 1000, 700));
        });

        viewJobsBtn.setOnAction(e -> {
            MOViewJobsView viewJobsView = new MOViewJobsView(stage, moUsername);
            stage.setScene(ThemeManager.createScene(viewJobsView.createContent(), 1000, 700));
        });

        reviewBtn.setOnAction(e -> {
            MOViewApplicationsView viewAppsView = new MOViewApplicationsView(stage, moUsername);
            stage.setScene(ThemeManager.createScene(viewAppsView.createContent(), 1000, 700));
        });

        backBtn.setOnAction(e -> {
            RoleSelectView roleSelectView = new RoleSelectView(stage);
            stage.setScene(ThemeManager.createScene(roleSelectView.createContent(), 1000, 700));
        });

        VBox card = new VBox(14, publishBtn, viewJobsBtn, reviewBtn, backBtn);
        card.getStyleClass().add("surface-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);

        VBox root = new VBox(20, hero, card);
        root.getStyleClass().add("app-page");
        root.setPadding(new Insets(36));
        root.setAlignment(Pos.TOP_CENTER);

        return root;
    }
}
