package com.group4.tarecruitment.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminView {

    private final Stage stage;

    public AdminView(Stage stage) {
        this.stage = stage;
    }

    public Parent createContent() {
        Label title = new Label("Admin Dashboard");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Manage system users, invite codes, and TA workloads.");
        subtitle.getStyleClass().add("page-subtitle");

        VBox hero = new VBox(6, title, subtitle);
        hero.getStyleClass().add("hero-card");
        hero.setMaxWidth(560);

        Button workloadBtn = new Button("Check TA Workload");
        Button manageBtn = new Button("Manage System");
        Button backBtn = new Button("Back to Role Selection");

        workloadBtn.getStyleClass().add("btn-primary");
        manageBtn.getStyleClass().add("btn-info");
        backBtn.getStyleClass().add("btn-muted");

        workloadBtn.setPrefWidth(260);
        manageBtn.setPrefWidth(260);
        backBtn.setPrefWidth(260);

        VBox card = new VBox(14, workloadBtn, manageBtn, backBtn);
        card.getStyleClass().add("surface-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(360);

        VBox root = new VBox(20, hero, card);
        root.getStyleClass().add("app-page");
        root.setPadding(new Insets(36));
        root.setAlignment(Pos.TOP_CENTER);

        return root;
    }
}
