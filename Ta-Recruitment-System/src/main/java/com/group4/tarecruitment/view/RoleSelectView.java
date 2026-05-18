package com.group4.tarecruitment.view;

import com.group4.tarecruitment.controller.AdminController;
import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.service.ApplicantService;
import com.group4.tarecruitment.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Builds the login and registration page for TA, MO, and Admin users.
 */
public class RoleSelectView {
    private final Stage stage;
    private AdminController adminController;
    private final AuthService authService = new AuthService();

    public RoleSelectView(Stage stage) {
        this.stage = stage;
    }

    /**
     * Creates the role selection, login, and registration page.
     *
     * @return role selection root node
     */
    public Parent createContent() {
        Label pageTitle = new Label("International School Teaching Assistant Recruitment");
        pageTitle.getStyleClass().add("login-title");

        Label pageSubtitle = new Label("Login or register with your username, password, and selected role.");
        pageSubtitle.getStyleClass().add("login-subtitle");

        VBox titleBox = new VBox(8, pageTitle, pageSubtitle);
        titleBox.setAlignment(Pos.TOP_LEFT);

        Label cardTitle = new Label("Account Login");
        cardTitle.getStyleClass().add("login-card-title");

        Label roleLabel = new Label("Role");
        roleLabel.getStyleClass().add("login-label");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("TA", "MO", "Admin");
        roleBox.setValue("TA");
        roleBox.getStyleClass().add("login-combo");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        Label usernameLabel = new Label("Account");
        usernameLabel.getStyleClass().add("login-label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.getStyleClass().add("login-input");
        usernameField.setMaxWidth(Double.MAX_VALUE);

        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("login-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.getStyleClass().add("login-input");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        Label inviteLabel = new Label("Invite Code");
        inviteLabel.getStyleClass().add("login-label");
        TextField inviteField = new TextField();
        inviteField.setPromptText("Enter invite code");
        inviteField.getStyleClass().add("login-input");
        inviteField.setMaxWidth(Double.MAX_VALUE);

        inviteLabel.setVisible(true);
        inviteLabel.setManaged(true);
        inviteField.setVisible(true);
        inviteField.setManaged(true);

        roleBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean needsCode = "TA".equals(newVal) || "MO".equals(newVal);
            inviteLabel.setVisible(needsCode);
            inviteLabel.setManaged(needsCode);
            inviteField.setVisible(needsCode);
            inviteField.setManaged(needsCode);
        });

        Label messageLabel = new Label();
        messageLabel.getStyleClass().addAll("status-label", "login-link");
        messageLabel.setWrapText(true);
        messageLabel.setMinHeight(38);

        Button loginBtn = new Button("login");
        loginBtn.getStyleClass().addAll("login-button", "primary-button");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Button registerBtn = new Button("Register");
        registerBtn.getStyleClass().addAll("login-button", "secondary-button");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        Hyperlink hintLink = new Hyperlink("No account? You need to click 'Register'.");
        hintLink.getStyleClass().add("login-link");
        hintLink.setFocusTraversable(false);

        loginBtn.setOnAction(e -> handleLogin(usernameField, passwordField, messageLabel, roleBox.getValue()));
        registerBtn.setOnAction(e -> handleRegister(usernameField, passwordField, inviteField, messageLabel, roleBox.getValue()));
        passwordField.setOnAction(e -> handleLogin(usernameField, passwordField, messageLabel, roleBox.getValue()));

        HBox buttonRow = new HBox(12, loginBtn, registerBtn);
        HBox.setHgrow(loginBtn, Priority.ALWAYS);
        HBox.setHgrow(registerBtn, Priority.ALWAYS);

        VBox loginCard = new VBox(12,
                cardTitle,
                roleLabel, roleBox,
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                inviteLabel, inviteField,
                buttonRow,
                messageLabel,
                hintLink
        );
        loginCard.getStyleClass().add("login-card");
        loginCard.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox contentRow = new HBox(40, loginCard, spacer);
        contentRow.setAlignment(Pos.CENTER_LEFT);

        VBox shell = new VBox(36, titleBox, contentRow);
        shell.getStyleClass().add("login-shell");
        shell.setAlignment(Pos.CENTER_LEFT);
        shell.setMaxWidth(Double.MAX_VALUE);

        StackPane overlay = new StackPane(shell);
        overlay.getStyleClass().addAll("login-page", "login-overlay");
        StackPane.setAlignment(shell, Pos.CENTER_LEFT);
        overlay.setPadding(new Insets(24));

        return overlay;
    }

    private void handleLogin(TextField usernameField, PasswordField passwordField, Label messageLabel, String role) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (role == null || role.isBlank()) {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("Please select a role before logging in.");
            return;
        }
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        boolean success = authService.login(username, password, role);
        if (!success) {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("Invalid " + role + " username or password.");
            return;
        }

        messageLabel.setText("");

        if (role.equals("TA")) {
            try {
                ApplicantService applicantService = new ApplicantService();
                Applicant existingApplicant = applicantService.getApplicantByUsername(username);

                if (existingApplicant != null) {
                    TAHomeView taHomeView = new TAHomeView(stage, existingApplicant);
                    stage.getScene().setRoot(taHomeView.createContent());
                    stage.setTitle("TA Dashboard");
                } else {
                    HelloView helloView = new HelloView(stage);
                    helloView.setLoginUsername(username);
                    stage.getScene().setRoot(helloView.createContent());
                    stage.setTitle("Create Profile");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setStyle("-fx-text-fill: #d64545;");
                messageLabel.setText("Error loading profile: " + ex.getMessage());
            }
        } else if (role.equals("MO")) {
            TeacherView teacherView = new TeacherView(stage, username);
            stage.getScene().setRoot(teacherView.createContent());
            stage.setTitle("MO Dashboard");
        } else if (role.equals("Admin")) {
            adminController = new AdminController(stage);
            adminController.showAdminPanel();
        }
    }

    private void handleRegister(TextField usernameField, PasswordField passwordField,
                                TextField inviteField, Label messageLabel, String role) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (role == null || role.isBlank()) {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("Please select a role before registering.");
            return;
        }

        if ("Admin".equals(role)) {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("Admin accounts cannot be self-registered. Contact a system administrator.");
            return;
        }

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("Username and password cannot be empty.");
            return;
        }

        if (!password.matches("^[A-Za-z0-9]{6,}$")) {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("Password must be at least 6 characters and contain only letters or digits.");
            return;
        }

        String inviteCode = inviteField.getText().trim();
        if (inviteCode.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("An invite code is required to register as " + role + ".");
            return;
        }

        boolean success = authService.register(username, password, role, inviteCode);
        if (success) {
            messageLabel.setStyle("-fx-text-fill: #2d8a52;");
            messageLabel.setText("Registration successful. You can now log in as " + role + ".");
        } else {
            messageLabel.setStyle("-fx-text-fill: #d64545;");
            messageLabel.setText("Registration failed: invalid or already-used invite code, or username already exists.");
        }
    }
}
