package com.group4.tarecruitment.controller;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.service.ApplicantService;
import com.group4.tarecruitment.view.TAHomeView;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

/**
 * Handles TA profile creation and profile editing actions from the JavaFX forms.
 */
public class HelloController {
    private final ApplicantService applicantService = new ApplicantService();
    private String currentLoginUsername;

    /**
     * Stores the username that was authenticated before the TA profile is created.
     *
     * @param username authenticated account name
     */
    public void setCurrentLoginUsername(String username) {
        this.currentLoginUsername = username;
    }

    /**
     * Creates a TA applicant profile from form controls and navigates to the TA dashboard
     * after the profile is saved.
     *
     * @param studentIdField field containing the student ID
     * @param nameField field containing the applicant name
     * @param emailField field containing the applicant email
     * @param coursesField field containing available teaching courses
     * @param cbJava Java skill checkbox
     * @param cbEnglish English skill checkbox
     * @param cbTeaching teaching skill checkbox
     * @param cbPython Python skill checkbox
     * @param cbOffice Office skill checkbox
     * @param contactField field containing contact number
     * @param resultLabel label used to display validation or save results
     * @param submitBtn submit button used to resolve the current stage
     */
    public void createProfile(TextField studentIdField, TextField nameField, TextField emailField,
                              TextField coursesField, CheckBox cbJava, CheckBox cbEnglish,
                              CheckBox cbTeaching, CheckBox cbPython, CheckBox cbOffice,
                              TextField contactField, Label resultLabel, Button submitBtn) {

        String studentId = studentIdField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String courses = coursesField.getText().trim();
        String contact = contactField.getText().trim();

        StringBuilder skillTags = new StringBuilder();
        if (cbJava.isSelected()) skillTags.append("Java,");
        if (cbEnglish.isSelected()) skillTags.append("English,");
        if (cbTeaching.isSelected()) skillTags.append("Teaching,");
        if (cbPython.isSelected()) skillTags.append("Python,");
        if (cbOffice.isSelected()) skillTags.append("Office,");
        String skillStr = skillTags.length() > 0 ? skillTags.substring(0, skillTags.length() - 1) : "";

        if (studentId.isEmpty()) {
            resultLabel.setStyle("-fx-text-fill: red;");
            resultLabel.setText("Error: Student ID cannot be empty!");
            return;
        }
        if (email.isEmpty()) {
            resultLabel.setStyle("-fx-text-fill: red;");
            resultLabel.setText("Error: Email cannot be empty!");
            return;
        }
        if (!email.contains("@")) {
            resultLabel.setStyle("-fx-text-fill: red;");
            resultLabel.setText("Error: Email must contain @ !");
            return;
        }
        if (skillStr.isEmpty()) {
            resultLabel.setStyle("-fx-text-fill: red;");
            resultLabel.setText("Error: Please select at least one skill tag!");
            return;
        }

        try {
            List<Applicant> allApplicants = applicantService.getAllApplicants();
            for (Applicant a : allApplicants) {
                if (a.getStudentId().equals(studentId)) {
                    resultLabel.setStyle("-fx-text-fill: red;");
                    resultLabel.setText("Error: This student ID is already registered!");
                    return;
                }
            }

            String taId = "TA-" + System.currentTimeMillis();

            Applicant applicant = new Applicant(taId, studentId, name, email, courses, skillStr, contact);
            applicant.setUsername(currentLoginUsername);

            applicantService.addApplicant(applicant);

            Stage stage = (Stage) submitBtn.getScene().getWindow();
            TAHomeView taHomeView = new TAHomeView(stage, applicant);
            stage.getScene().setRoot(taHomeView.createContent());
            stage.setTitle("TA Dashboard");

        } catch (Exception e) {
            resultLabel.setStyle("-fx-text-fill: red;");
            resultLabel.setText("Creation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing TA profile based on form input.
     *
     * @param studentIdField field used to locate the existing applicant
     * @param nameField optional new applicant name
     * @param emailField optional new applicant email
     * @param coursesField optional new available courses
     * @param cbJava Java skill checkbox
     * @param cbEnglish English skill checkbox
     * @param cbTeaching teaching skill checkbox
     * @param cbPython Python skill checkbox
     * @param cbOffice Office skill checkbox
     * @param contactField optional new contact number
     * @param resultLabel label used to display update results
     */
    public void editProfile(TextField studentIdField, TextField nameField, TextField emailField,
                            TextField coursesField, CheckBox cbJava, CheckBox cbEnglish,
                            CheckBox cbTeaching, CheckBox cbPython, CheckBox cbOffice,
                            TextField contactField, Label resultLabel) {

        String studentId = studentIdField.getText().trim();
        if (studentId.isEmpty()) {
            resultLabel.setStyle("-fx-text-fill: red;");
            resultLabel.setText("Error: Please enter student ID!");
            return;
        }

        try {
            Applicant applicant = applicantService.getApplicantByStudentId(studentId);
            if (applicant == null) {
                resultLabel.setStyle("-fx-text-fill: red;");
                resultLabel.setText("Error: Applicant not found!");
                return;
            }

            String name = nameField.getText().trim().isEmpty() ? applicant.getName() : nameField.getText().trim();
            String email = emailField.getText().trim().isEmpty() ? applicant.getEmail() : emailField.getText().trim();
            String courses = coursesField.getText().trim().isEmpty() ? applicant.getCourses() : coursesField.getText().trim();
            String contact = contactField.getText().trim().isEmpty() ? applicant.getContact() : contactField.getText().trim();

            StringBuilder skillTags = new StringBuilder();
            if (cbJava.isSelected() || cbEnglish.isSelected() || cbTeaching.isSelected() || cbPython.isSelected() || cbOffice.isSelected()) {
                if (cbJava.isSelected()) skillTags.append("Java,");
                if (cbEnglish.isSelected()) skillTags.append("English,");
                if (cbTeaching.isSelected()) skillTags.append("Teaching,");
                if (cbPython.isSelected()) skillTags.append("Python,");
                if (cbOffice.isSelected()) skillTags.append("Office,");
            } else {
                skillTags.append(applicant.getSkillTags());
            }
            String skillStr = skillTags.length() > 0 ? skillTags.substring(0, skillTags.length() - 1) : "";

            if (!email.equals(applicant.getEmail()) && !email.contains("@")) {
                resultLabel.setStyle("-fx-text-fill: red;");
                resultLabel.setText("Error: Invalid new email format!");
                return;
            }

            applicant.setName(name);
            applicant.setEmail(email);
            applicant.setCourses(courses);
            applicant.setSkillTags(skillStr);
            applicant.setContact(contact);

            applicantService.updateApplicant(applicant);

            resultLabel.setStyle("-fx-text-fill: green;");
            resultLabel.setText("Profile updated successfully!");

            clearFields(studentIdField, nameField, emailField, coursesField,
                    cbJava, cbEnglish, cbTeaching, cbPython, cbOffice, contactField);

        } catch (Exception e) {
            resultLabel.setStyle("-fx-text-fill: red;");
            resultLabel.setText("Update failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields(TextField studentIdField, TextField nameField, TextField emailField,
                             TextField coursesField, CheckBox cbJava, CheckBox cbEnglish,
                             CheckBox cbTeaching, CheckBox cbPython, CheckBox cbOffice,
                             TextField contactField) {
        studentIdField.clear();
        nameField.clear();
        emailField.clear();
        coursesField.clear();
        cbJava.setSelected(false);
        cbEnglish.setSelected(false);
        cbTeaching.setSelected(false);
        cbPython.setSelected(false);
        cbOffice.setSelected(false);
        contactField.clear();
    }
}
