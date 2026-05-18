package com.group4.tarecruitment.view;

import com.group4.tarecruitment.controller.HelloController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Builds the first-time TA profile creation page.
 */
public class HelloView {
    private final Stage stage;
    private final HelloController controller;
    private String loginUsername;

    public HelloView(Stage stage) {
        this.stage = stage;
        this.controller = new HelloController();
    }

    /**
     * Stores the username associated with the profile being created.
     *
     * @param username authenticated account username
     */
    public void setLoginUsername(String username) {
        this.loginUsername = username;
    }

    /**
     * Creates the TA profile form page.
     *
     * @return profile creation root node
     */
    public Parent createContent() {
        VBox pageRoot = new VBox(18);
        pageRoot.getStyleClass().add("form-page");
        pageRoot.setAlignment(Pos.TOP_LEFT);
        pageRoot.setPadding(new Insets(36));

        Label title = new Label("Create TA Application Profile");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Complete your basic profile before browsing and applying for positions.");
        subtitle.getStyleClass().add("page-subtitle");

        VBox infoCard = new VBox(14);
        infoCard.getStyleClass().add("form-card");
        infoCard.setPadding(new Insets(24));

        String labelStyle = "-fx-font-size: 14px; -fx-text-fill: #33415e; -fx-font-weight: bold;";
        String fieldStyle = "-fx-font-size: 14px;";
        String errorFieldStyle = "-fx-font-size: 14px; -fx-border-color: #e74c3c; -fx-background-color: #fff2f2;";

        Label studentIdLabel = new Label("Student ID:");
        studentIdLabel.setStyle(labelStyle);
        TextField studentIdField = new TextField();
        studentIdField.setStyle(fieldStyle);
        studentIdField.setMaxWidth(Double.MAX_VALUE);

        Label nameLabel = new Label("Name:");
        nameLabel.setStyle(labelStyle);
        TextField nameField = new TextField();
        nameField.setStyle(fieldStyle);
        nameField.setMaxWidth(Double.MAX_VALUE);

        Label emailLabel = new Label("Email:");
        emailLabel.setStyle(labelStyle);
        TextField emailField = new TextField();
        emailField.setStyle(fieldStyle);
        emailField.setMaxWidth(Double.MAX_VALUE);

        Label coursesLabel = new Label("Courses Available to Teach:");
        coursesLabel.setStyle(labelStyle);
        TextField coursesField = new TextField();
        coursesField.setStyle(fieldStyle);
        coursesField.setMaxWidth(Double.MAX_VALUE);

        Label skillsLabel = new Label("Skill Tags:");
        skillsLabel.setStyle(labelStyle);
        CheckBox cbJava = new CheckBox("Java");
        CheckBox cbEnglish = new CheckBox("English");
        CheckBox cbTeaching = new CheckBox("Teaching");
        CheckBox cbPython = new CheckBox("Python");
        CheckBox cbOffice = new CheckBox("Office");

        VBox skillsBox = new VBox(6, cbJava, cbEnglish, cbTeaching, cbPython, cbOffice);

        Label contactLabel = new Label("Contact Number:");
        contactLabel.setStyle(labelStyle);
        TextField contactField = new TextField();
        contactField.setStyle(fieldStyle);
        contactField.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setAlignment(Pos.CENTER_LEFT);
        grid.add(studentIdLabel, 0, 0);
        grid.add(studentIdField, 1, 0);
        grid.add(nameLabel, 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(emailLabel, 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(coursesLabel, 0, 3);
        grid.add(coursesField, 1, 3);
        grid.add(skillsLabel, 0, 4);
        grid.add(skillsBox, 1, 4);
        grid.add(contactLabel, 0, 5);
        grid.add(contactField, 1, 5);

        GridPane.setFillWidth(studentIdField, true);
        GridPane.setFillWidth(nameField, true);
        GridPane.setFillWidth(emailField, true);
        GridPane.setFillWidth(coursesField, true);
        GridPane.setFillWidth(contactField, true);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(new ColumnConstraints(), col2);

        Button submitBtn = new Button("Create Profile");
        submitBtn.getStyleClass().add("primary-button");

        Label resultLabel = new Label("");
        resultLabel.getStyleClass().add("status-label");

        controller.setCurrentLoginUsername(loginUsername);

        submitBtn.setOnAction(e -> {
            boolean isFormValid = true;
            resultLabel.setText("");

            if (studentIdField.getText().trim().isEmpty()) {
                resultLabel.setText(resultLabel.getText() + "❌ Student ID is required.\n");
                studentIdField.setStyle(errorFieldStyle);
                isFormValid = false;
            } else {
                studentIdField.setStyle(fieldStyle);
            }

            if (nameField.getText().trim().isEmpty()) {
                resultLabel.setText(resultLabel.getText() + "❌ Name is required.\n");
                nameField.setStyle(errorFieldStyle);
                isFormValid = false;
            } else {
                nameField.setStyle(fieldStyle);
            }

            if (emailField.getText().trim().isEmpty()) {
                resultLabel.setText(resultLabel.getText() + "❌ Email is required.\n");
                emailField.setStyle(errorFieldStyle);
                isFormValid = false;
            } else {
                emailField.setStyle(fieldStyle);
            }

            if (contactField.getText().trim().isEmpty()) {
                resultLabel.setText(resultLabel.getText() + "❌ Contact Number is required.\n");
                contactField.setStyle(errorFieldStyle);
                isFormValid = false;
            } else {
                contactField.setStyle(fieldStyle);
            }

            String studentId = studentIdField.getText().trim();
            if (isFormValid && !studentId.matches("\\d{10}")) {
                resultLabel.setText(resultLabel.getText() + "❌ Student ID must be 10 digits.\n");
                studentIdField.setStyle(errorFieldStyle);
                isFormValid = false;
            }

            String contact = contactField.getText().trim();
            if (isFormValid) {
                if (!contact.matches("^1[3-9]\\d{9}$") && !contact.matches("^\\d{3,4}-\\d{7,8}$")) {
                    resultLabel.setText(resultLabel.getText() + "❌ Invalid contact number format.\n");
                    contactField.setStyle(errorFieldStyle);
                    isFormValid = false;
                }
            }

            if (isFormValid) {
                resultLabel.setStyle("-fx-text-fill: #2d8a52; -fx-font-size: 14px;");
                resultLabel.setText("✅ Verifying...");

                controller.createProfile(
                        studentIdField, nameField, emailField,
                        coursesField, cbJava, cbEnglish, cbTeaching, cbPython, cbOffice,
                        contactField, resultLabel, submitBtn
                );
            } else {
                resultLabel.setStyle("-fx-text-fill: #d64545; -fx-font-size: 14px;");
            }
        });

        infoCard.getChildren().add(grid);
        VBox btnBox = new VBox(12, submitBtn, resultLabel);
        btnBox.setPadding(new Insets(6, 0, 0, 0));
        btnBox.setAlignment(Pos.CENTER_LEFT);

        pageRoot.getChildren().addAll(title, subtitle, infoCard, btnBox);
        return pageRoot;
    }
}
