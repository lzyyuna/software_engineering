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

    public void setLoginUsername(String username) {
        this.loginUsername = username;
    }

    public Parent createContent() {
        VBox pageRoot = new VBox(18);
        pageRoot.getStyleClass().add("app-page");
        pageRoot.setAlignment(Pos.TOP_CENTER);
        pageRoot.setPadding(new Insets(36));

        Label title = new Label("Create TA Application Profile");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Complete your basic profile before browsing and applying for positions.");
        subtitle.getStyleClass().add("page-subtitle");

        VBox header = new VBox(6, title, subtitle);
        header.setAlignment(Pos.TOP_LEFT);
        header.setMaxWidth(640);

        VBox infoCard = new VBox(14);
        infoCard.getStyleClass().add("surface-card");
        infoCard.setMaxWidth(640);

        Label studentIdLabel = labelOf("Student ID:");
        TextField studentIdField = new TextField();
        studentIdField.setMaxWidth(Double.MAX_VALUE);

        Label nameLabel = labelOf("Name:");
        TextField nameField = new TextField();
        nameField.setMaxWidth(Double.MAX_VALUE);

        Label emailLabel = labelOf("Email:");
        TextField emailField = new TextField();
        emailField.setMaxWidth(Double.MAX_VALUE);

        Label coursesLabel = labelOf("Courses Available to Teach:");
        TextField coursesField = new TextField();
        coursesField.setMaxWidth(Double.MAX_VALUE);

        Label skillsLabel = labelOf("Skill Tags:");
        CheckBox cbJava = new CheckBox("Java");
        CheckBox cbEnglish = new CheckBox("English");
        CheckBox cbTeaching = new CheckBox("Teaching");
        CheckBox cbPython = new CheckBox("Python");
        CheckBox cbOffice = new CheckBox("Office");
        VBox skillsBox = new VBox(6, cbJava, cbEnglish, cbTeaching, cbPython, cbOffice);

        Label contactLabel = labelOf("Contact Number:");
        TextField contactField = new TextField();
        contactField.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setAlignment(Pos.CENTER_LEFT);
        grid.add(studentIdLabel, 0, 0); grid.add(studentIdField, 1, 0);
        grid.add(nameLabel,      0, 1); grid.add(nameField,      1, 1);
        grid.add(emailLabel,     0, 2); grid.add(emailField,     1, 2);
        grid.add(coursesLabel,   0, 3); grid.add(coursesField,   1, 3);
        grid.add(skillsLabel,    0, 4); grid.add(skillsBox,      1, 4);
        grid.add(contactLabel,   0, 5); grid.add(contactField,   1, 5);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(new ColumnConstraints(), col2);

        Button submitBtn = new Button("Create Profile");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setPrefWidth(180);

        Label resultLabel = new Label("");
        resultLabel.getStyleClass().add("status-label");
        resultLabel.setWrapText(true);

        controller.setCurrentLoginUsername(loginUsername);

        submitBtn.setOnAction(e -> {
            boolean isFormValid = true;
            resultLabel.setText("");

            for (TextField f : new TextField[]{studentIdField, nameField, emailField, contactField}) {
                f.getStyleClass().remove("field-error");
            }

            if (studentIdField.getText().trim().isEmpty()) {
                resultLabel.setText(resultLabel.getText() + "Student ID is required.\n");
                markError(studentIdField);
                isFormValid = false;
            }
            if (nameField.getText().trim().isEmpty()) {
                resultLabel.setText(resultLabel.getText() + "Name is required.\n");
                markError(nameField);
                isFormValid = false;
            }
            if (emailField.getText().trim().isEmpty()) {
                resultLabel.setText(resultLabel.getText() + "Email is required.\n");
                markError(emailField);
                isFormValid = false;
            }
            if (contactField.getText().trim().isEmpty()) {
                resultLabel.setText(resultLabel.getText() + "Contact Number is required.\n");
                markError(contactField);
                isFormValid = false;
            }

            String studentId = studentIdField.getText().trim();
            if (isFormValid && !studentId.matches("\\d{10}")) {
                resultLabel.setText(resultLabel.getText() + "Student ID must be 10 digits.\n");
                markError(studentIdField);
                isFormValid = false;
            }

            String contact = contactField.getText().trim();
            if (isFormValid
                    && !contact.matches("^1[3-9]\\d{9}$")
                    && !contact.matches("^\\d{3,4}-\\d{7,8}$")) {
                resultLabel.setText(resultLabel.getText() + "Invalid contact number format.\n");
                markError(contactField);
                isFormValid = false;
            }

            resultLabel.getStyleClass().removeAll("status-error", "status-success");
            if (isFormValid) {
                resultLabel.getStyleClass().add("status-success");
                resultLabel.setText("Verifying...");
                controller.createProfile(
                        studentIdField, nameField, emailField,
                        coursesField, cbJava, cbEnglish, cbTeaching, cbPython, cbOffice,
                        contactField, resultLabel, submitBtn
                );
            } else {
                resultLabel.getStyleClass().add("status-error");
            }
        });

        infoCard.getChildren().add(grid);

        VBox btnBox = new VBox(12, submitBtn, resultLabel);
        btnBox.setPadding(new Insets(6, 0, 0, 0));
        btnBox.setAlignment(Pos.CENTER_LEFT);
        btnBox.setMaxWidth(640);

        pageRoot.getChildren().addAll(header, infoCard, btnBox);
        return pageRoot;
    }

    private Label labelOf(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("login-label");
        return l;
    }

    private void markError(TextField field) {
        if (!field.getStyleClass().contains("field-error")) {
            field.getStyleClass().add("field-error");
        }
    }
}
