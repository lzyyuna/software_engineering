package com.group4.tarecruitment.view;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.service.ApplicantService;
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

/**
 * Builds the TA profile detail and profile edit pages.
 */
public class ProfileDetailView {
    private final Applicant applicant;
    private final Stage stage;
    private final ApplicantService applicantService = new ApplicantService();

    public ProfileDetailView(Applicant applicant, Stage stage) {
        this.applicant = applicant;
        this.stage = stage;
    }

    public Parent getView() {
        return createDisplayView();
    }

    private Parent createDisplayView() {
        Label title = new Label("Profile Details");
        title.getStyleClass().add("page-title");

        VBox infoCard = new VBox(14);
        infoCard.getStyleClass().add("surface-card");
        infoCard.setMaxWidth(720);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(24);
        infoGrid.setVgap(14);
        infoGrid.setAlignment(Pos.CENTER_LEFT);

        addInfoRow(infoGrid, 0, "TA ID:",             applicant.getTaId());
        addInfoRow(infoGrid, 1, "Student ID:",        applicant.getStudentId());
        addInfoRow(infoGrid, 2, "Name:",              applicant.getName());
        addInfoRow(infoGrid, 3, "Email:",             applicant.getEmail());
        addInfoRow(infoGrid, 4, "Available Courses:", applicant.getCourses());
        addInfoRow(infoGrid, 5, "Skills:",            applicant.getSkillTags());
        addInfoRow(infoGrid, 6, "Phone:",             applicant.getContact());

        infoCard.getChildren().add(infoGrid);

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-success");
        editBtn.setOnAction(e -> {
            stage.getScene().setRoot(createEditView());
            stage.setTitle("Profile Details");
        });

        Button backToHomeBtn = new Button("Back to TA Home");
        backToHomeBtn.getStyleClass().add("btn-info");
        backToHomeBtn.setOnAction(e -> {
            TAHomeView taHomeView = new TAHomeView(stage, applicant);
            stage.getScene().setRoot(taHomeView.createContent());
            stage.setTitle("TA Dashboard");
        });

        HBox buttonBox = new HBox(14, editBtn, backToHomeBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, title, infoCard, buttonBox);
        root.getStyleClass().add("app-page");
        root.setPadding(new Insets(36));
        root.setAlignment(Pos.TOP_CENTER);

        return root;
    }

    private Parent createEditView() {
        Label title = new Label("Edit Profile");
        title.getStyleClass().add("page-title");

        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("surface-card");
        formCard.setMaxWidth(720);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(18);
        formGrid.setVgap(14);
        formGrid.setAlignment(Pos.CENTER_LEFT);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(150);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(col1, col2);

        TextField taIdField = new TextField(safe(applicant.getTaId()));
        taIdField.setEditable(false);
        taIdField.setDisable(true);

        TextField studentIdField = new TextField(safe(applicant.getStudentId()));
        TextField nameField = new TextField(safe(applicant.getName()));
        TextField emailField = new TextField(safe(applicant.getEmail()));
        TextField coursesField = new TextField(safe(applicant.getCourses()));
        TextField contactField = new TextField(safe(applicant.getContact()));

        CheckBox cbJava = new CheckBox("Java");
        CheckBox cbEnglish = new CheckBox("English");
        CheckBox cbTeaching = new CheckBox("Teaching");
        CheckBox cbPython = new CheckBox("Python");
        CheckBox cbOffice = new CheckBox("Office");

        String oldSkills = safe(applicant.getSkillTags()).toLowerCase();
        cbJava.setSelected(oldSkills.contains("java"));
        cbEnglish.setSelected(oldSkills.contains("english"));
        cbTeaching.setSelected(oldSkills.contains("teaching"));
        cbPython.setSelected(oldSkills.contains("python"));
        cbOffice.setSelected(oldSkills.contains("office"));

        VBox skillsBox = new VBox(6, cbJava, cbEnglish, cbTeaching, cbPython, cbOffice);

        addFormRow(formGrid, 0, "TA ID:",              taIdField);
        addFormRow(formGrid, 1, "Student ID:",         studentIdField);
        addFormRow(formGrid, 2, "Name:",               nameField);
        addFormRow(formGrid, 3, "Email:",              emailField);
        addFormRow(formGrid, 4, "Available Courses:",  coursesField);
        addFormRow(formGrid, 5, "Skills:",             skillsBox);
        addFormRow(formGrid, 6, "Phone:",              contactField);

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);
        resultLabel.getStyleClass().add("status-label");

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("btn-success");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-muted");

        Button backToHomeBtn = new Button("Back to TA Home");
        backToHomeBtn.getStyleClass().add("btn-info");

        saveBtn.setOnAction(e -> {
            boolean isFormValid = true;
            resultLabel.setText("");
            resultLabel.getStyleClass().removeAll("status-error", "status-success");

            for (TextField f : new TextField[]{studentIdField, nameField, emailField, contactField}) {
                f.getStyleClass().remove("field-error");
            }

            String studentId = studentIdField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String courses = coursesField.getText().trim();
            String contact = contactField.getText().trim();

            StringBuilder errors = new StringBuilder();

            if (studentId.isEmpty()) { errors.append("Student ID cannot be empty.\n"); markError(studentIdField); isFormValid = false; }
            if (name.isEmpty())      { errors.append("Name cannot be empty.\n");      markError(nameField);      isFormValid = false; }
            if (email.isEmpty())     { errors.append("Email cannot be empty.\n");     markError(emailField);     isFormValid = false; }
            if (contact.isEmpty())   { errors.append("Phone cannot be empty.\n");     markError(contactField);   isFormValid = false; }

            if (!studentId.isEmpty() && !studentId.matches("\\d{10}")) {
                errors.append("Student ID must be 10 digits.\n");
                markError(studentIdField);
                isFormValid = false;
            }
            if (!email.isEmpty() && !email.contains("@")) {
                errors.append("Invalid email format. Email must contain @.\n");
                markError(emailField);
                isFormValid = false;
            }
            if (!contact.isEmpty()
                    && !contact.matches("^1[3-9]\\d{9}$")
                    && !contact.matches("^\\d{3,4}-\\d{7,8}$")) {
                errors.append("Invalid phone number format.\n");
                markError(contactField);
                isFormValid = false;
            }

            StringBuilder skillTags = new StringBuilder();
            if (cbJava.isSelected())     skillTags.append("Java,");
            if (cbEnglish.isSelected())  skillTags.append("English,");
            if (cbTeaching.isSelected()) skillTags.append("Teaching,");
            if (cbPython.isSelected())   skillTags.append("Python,");
            if (cbOffice.isSelected())   skillTags.append("Office,");

            if (skillTags.length() == 0) {
                errors.append("Please select at least one skill.\n");
                isFormValid = false;
            }

            if (!isFormValid) {
                resultLabel.setText(errors.toString().trim());
                resultLabel.getStyleClass().add("status-error");
                return;
            }

            String skillStr = skillTags.substring(0, skillTags.length() - 1);

            try {
                applicant.setStudentId(studentId);
                applicant.setName(name);
                applicant.setEmail(email);
                applicant.setCourses(courses);
                applicant.setSkillTags(skillStr);
                applicant.setContact(contact);

                applicantService.updateApplicant(applicant);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Profile updated successfully.");
                alert.showAndWait();

                stage.getScene().setRoot(createDisplayView());
                stage.setTitle("Profile Details");
            } catch (Exception ex) {
                resultLabel.getStyleClass().add("status-error");
                resultLabel.setText("Save failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        cancelBtn.setOnAction(e -> {
            stage.getScene().setRoot(createDisplayView());
            stage.setTitle("Profile Details");
        });

        backToHomeBtn.setOnAction(e -> {
            TAHomeView taHomeView = new TAHomeView(stage, applicant);
            stage.getScene().setRoot(taHomeView.createContent());
            stage.setTitle("TA Dashboard");
        });

        HBox buttonBox = new HBox(14, saveBtn, cancelBtn, backToHomeBtn);
        buttonBox.setAlignment(Pos.CENTER);

        formCard.getChildren().addAll(formGrid, resultLabel);

        VBox root = new VBox(18, title, formCard, buttonBox);
        root.getStyleClass().add("app-page");
        root.setPadding(new Insets(28, 40, 28, 40));
        root.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("app-page");

        return scrollPane;
    }

    private void addInfoRow(GridPane grid, int row, String labelText, String valueText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("profile-info-label");

        Label value = new Label(valueText == null || valueText.isBlank() ? "Not Provided" : valueText);
        value.getStyleClass().add("profile-info-value");

        grid.add(label, 0, row);
        grid.add(value, 1, row);
    }

    private void addFormRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("login-label");
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        if (field instanceof Control c) c.setMaxWidth(Double.MAX_VALUE);
    }

    private void markError(TextField field) {
        if (!field.getStyleClass().contains("field-error")) {
            field.getStyleClass().add("field-error");
        }
    }

    private String safe(String value) { return value == null ? "" : value; }
}
