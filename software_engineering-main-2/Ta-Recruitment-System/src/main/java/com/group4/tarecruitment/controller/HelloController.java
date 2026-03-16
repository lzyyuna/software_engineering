package com.group4.tarecruitment.controller;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.service.ApplicantService;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class HelloController {

    private final ApplicantService applicantService = new ApplicantService();

    public void handleRegistration(TextField nameField, TextField emailField, TextField skillsField, Label resultLabel) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String skills = skillsField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            resultLabel.setText("Error: Name & Email cannot be empty!");
            return;
        }
        if (!email.contains("@")) {
            resultLabel.setText("Error: Invalid email format!");
            return;
        }

        String id = "TA-" + System.currentTimeMillis();
        Applicant applicant = new Applicant(id, name, email, skills);

        try {
            applicantService.addApplicant(applicant);
            resultLabel.setText("SUCCESS! Registered: " + id);
            nameField.clear();
            emailField.clear();
            skillsField.clear();
        } catch (Exception e) {
            resultLabel.setText("Save failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}