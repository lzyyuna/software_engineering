package com.group4.tarecruitment.view;

import com.group4.tarecruitment.model.Application;
import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.model.Job;
import com.group4.tarecruitment.model.SkillMatchResult;
import com.group4.tarecruitment.service.AISkillMatchService;
import com.group4.tarecruitment.service.ApplicantService;
import com.group4.tarecruitment.service.MOService;
import com.group4.tarecruitment.service.SkillMatchService;
import com.group4.tarecruitment.util.ThemeManager;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MOViewApplicationsView {

    private final Stage stage;
    private final String moName;
    private final MOService moService = new MOService();
    private final ApplicantService applicantService = new ApplicantService();
    private final SkillMatchService skillMatchService = new SkillMatchService();

    private final AISkillMatchService aiSkillMatchService;
    private final String apiKey;

    private VBox appListBox;
    private ObservableList<Application> appData;
    private ComboBox<Job> jobComboBox;
    private ComboBox<String> statusFilter;
    private ComboBox<String> sortByCombo;
    private CheckBox strongMatchCb;

    private final int PAGE_SIZE = 3;
    private int currentPage = 1;

    public MOViewApplicationsView(Stage stage, String moName) {
        this.stage = stage;
        this.moName = moName;
        this.apiKey = AISkillMatchService.getApiKeyFromEnv();
        this.aiSkillMatchService = (apiKey != null) ? new AISkillMatchService(apiKey) : null;
    }

    public Parent createContent() {
        Label title = new Label("Review TA Applications");
        title.getStyleClass().add("page-title");

        Button refreshBtn = new Button("Refresh List");
        Button backBtn = new Button("Back to Home");
        refreshBtn.getStyleClass().add("btn-primary");
        backBtn.getStyleClass().add("btn-muted");

        HBox topBar = new HBox(10, refreshBtn, backBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("toolbar");

        Label jobLabel = new Label("Select Position:");
        jobComboBox = new ComboBox<>();
        jobComboBox.setPrefWidth(200);
        jobComboBox.setPromptText("Select a position");

        Label statusLabel = new Label("Status:");
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Pending", "Approved", "Rejected");
        statusFilter.setValue("All");
        statusFilter.setPrefWidth(120);

        Label sortLabel = new Label("Sort by:");
        sortByCombo = new ComboBox<>();
        sortByCombo.getItems().addAll("Match Score (Desc)", "Application Time (Desc)", "Application Time (Asc)");
        sortByCombo.setValue("Match Score (Desc)");
        sortByCombo.setPrefWidth(180);

        strongMatchCb = new CheckBox("Only show strong matches");

        Button resetBtn = new Button("Reset");
        resetBtn.getStyleClass().add("btn-muted");

        HBox filterSpacer = new HBox();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);

        HBox filterBar = new HBox(12, jobLabel, jobComboBox, statusLabel, statusFilter,
                sortLabel, sortByCombo, strongMatchCb, filterSpacer, resetBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("filter-bar");

        appListBox = new VBox(12);
        appListBox.getStyleClass().add("list-container");

        HBox pageBox = new HBox(10);
        Button prevBtn = new Button("Previous");
        Button nextBtn = new Button("Next");
        Label pageLabel = new Label("Page 1");
        pageBox.getChildren().addAll(prevBtn, pageLabel, nextBtn);
        pageBox.setAlignment(Pos.CENTER);

        loadJobs();

        refreshBtn.setOnAction(e -> { currentPage = 1; loadApplications(pageLabel); });
        resetBtn.setOnAction(e -> {
            statusFilter.setValue("All");
            sortByCombo.setValue("Match Score (Desc)");
            strongMatchCb.setSelected(false);
            currentPage = 1;
            loadApplications(pageLabel);
        });
        backBtn.setOnAction(e -> {
            TeacherView teacherView = new TeacherView(stage, moName);
            stage.setScene(ThemeManager.createScene(teacherView.createContent(), 1000, 700));
        });
        prevBtn.setOnAction(e -> { if (currentPage > 1) { currentPage--; loadApplications(pageLabel); } });
        nextBtn.setOnAction(e -> {
            try {
                int totalPages = getTotalPages();
                if (currentPage < totalPages) { currentPage++; loadApplications(pageLabel); }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        jobComboBox.setOnAction(e -> { currentPage = 1; loadApplications(pageLabel); });
        statusFilter.setOnAction(e -> { currentPage = 1; loadApplications(pageLabel); });
        sortByCombo.setOnAction(e -> { currentPage = 1; loadApplications(pageLabel); });
        strongMatchCb.setOnAction(e -> { currentPage = 1; loadApplications(pageLabel); });

        VBox root = new VBox(14, title, topBar, filterBar, appListBox, new Separator(), pageBox);
        root.getStyleClass().add("app-page");
        root.setPadding(new Insets(24));

        return root;
    }

    private void loadJobs() {
        try {
            List<Job> jobs = moService.getMyPostedJobs(moName);
            jobComboBox.getItems().clear();
            jobComboBox.getItems().addAll(jobs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getTotalPages() throws Exception {
        List<Application> filteredApps = getFilteredApplications();
        if (filteredApps.isEmpty()) return 1;
        return (int) Math.ceil((double) filteredApps.size() / PAGE_SIZE);
    }

    private List<Application> getFilteredApplications() throws Exception {
        Job selectedJob = jobComboBox.getValue();
        if (selectedJob == null) return new ArrayList<>();

        List<Application> apps = moService.getJobApplications(selectedJob.getJobId(), moName);

        String filter = statusFilter.getValue();
        if (!"All".equals(filter)) {
            apps = apps.stream().filter(a -> a.getStatus().equals(filter)).toList();
        }

        if (strongMatchCb.isSelected()) {
            apps = apps.stream().filter(app -> {
                try {
                    Applicant applicant = applicantService.getApplicantById(app.getTaId());
                    SkillMatchResult match = skillMatchService.match(applicant, selectedJob);
                    return "Strong Match".equals(match.getRecommendationLevel());
                } catch (Exception e) {
                    return false;
                }
            }).toList();
        }

        String sortBy = sortByCombo.getValue();
        if ("Match Score (Desc)".equals(sortBy)) {
            apps = new ArrayList<>(apps);
            apps.sort((a1, a2) -> {
                try {
                    Applicant app1 = applicantService.getApplicantById(a1.getTaId());
                    Applicant app2 = applicantService.getApplicantById(a2.getTaId());
                    double s1 = skillMatchService.match(app1, selectedJob).getMatchScore();
                    double s2 = skillMatchService.match(app2, selectedJob).getMatchScore();
                    return Double.compare(s2, s1);
                } catch (Exception e) {
                    return 0;
                }
            });
        } else if ("Application Time (Desc)".equals(sortBy)) {
            apps = new ArrayList<>(apps);
            apps.sort((a1, a2) -> a2.getApplicationTime().compareTo(a1.getApplicationTime()));
        } else if ("Application Time (Asc)".equals(sortBy)) {
            apps = new ArrayList<>(apps);
            apps.sort((a1, a2) -> a1.getApplicationTime().compareTo(a2.getApplicationTime()));
        }

        return apps;
    }

    private void loadApplications(Label pageLabel) {
        appListBox.getChildren().clear();

        Job selectedJob = jobComboBox.getValue();
        if (selectedJob == null) {
            Label selectJobLabel = new Label("Please select a position first.");
            selectJobLabel.getStyleClass().add("empty-text");
            appListBox.getChildren().add(selectJobLabel);
            pageLabel.setText("Page 1 / 1");
            return;
        }

        try {
            List<Application> filteredApps = getFilteredApplications();
            int totalPages = filteredApps.isEmpty() ? 1 : (int) Math.ceil((double) filteredApps.size() / PAGE_SIZE);

            if (currentPage > totalPages) currentPage = totalPages;
            if (currentPage < 1) currentPage = 1;

            int start = (currentPage - 1) * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, filteredApps.size());

            List<Application> pageApps = filteredApps.isEmpty() ? new ArrayList<>() : filteredApps.subList(start, end);

            if (pageApps.isEmpty()) {
                Label emptyLabel = new Label("No Matched Applications");
                emptyLabel.getStyleClass().add("empty-text");
                appListBox.getChildren().add(emptyLabel);
                pageLabel.setText("Page 1 / 1");
                return;
            }

            for (Application app : pageApps) {
                Applicant applicant = applicantService.getApplicantById(app.getTaId());
                SkillMatchResult match = skillMatchService.match(applicant, selectedJob);

                VBox card = new VBox(8);
                card.getStyleClass().add("list-item-card");

                Label taIdLabel = new Label("TA: " + safe(app.getTaId())
                        + "  ·  " + safe(applicant != null ? applicant.getName() : "Unknown"));
                taIdLabel.getStyleClass().add("section-title");

                Label statusBadge = new Label(safe(app.getStatus()));
                statusBadge.getStyleClass().addAll("badge", statusBadgeClass(app.getStatus()));

                HBox headerSpacer = new HBox();
                HBox.setHgrow(headerSpacer, Priority.ALWAYS);

                HBox headerRow = new HBox(12, taIdLabel, headerSpacer, statusBadge);
                headerRow.setAlignment(Pos.CENTER_LEFT);

                Label emailLabel = new Label("Email: " + safe(applicant != null ? applicant.getEmail() : "N/A"));
                Label phoneLabel = new Label("Phone: " + safe(applicant != null ? applicant.getPhone() : "N/A"));
                Label timeLabel  = new Label("Applied: " + safe(app.getApplicationTime()));
                emailLabel.getStyleClass().add("muted-text");
                phoneLabel.getStyleClass().add("muted-text");
                timeLabel.getStyleClass().add("muted-text");

                HBox infoRow = new HBox(20, emailLabel, phoneLabel, timeLabel);
                infoRow.setAlignment(Pos.CENTER_LEFT);

                Label matchScoreLabel = new Label("Match: " + String.format("%.1f", match.getMatchScore()) + "%");
                matchScoreLabel.getStyleClass().addAll("badge", "badge-success");

                Label recommendationLabel = new Label(safe(match.getRecommendationLevel()));
                recommendationLabel.getStyleClass().addAll("badge", recommendationBadgeClass(match.getRecommendationLevel()));

                HBox matchRow = new HBox(12, matchScoreLabel, recommendationLabel);
                matchRow.setAlignment(Pos.CENTER_LEFT);

                Label matchedSkillsLabel = new Label("Matched Skills: " + formatList(match.getMatchedSkills()));
                Label missingSkillsLabel = new Label("Missing Skills: " + formatList(match.getMissingSkills()));
                missingSkillsLabel.getStyleClass().add("muted-text");

                VBox matchSection = new VBox(6, matchRow, matchedSkillsLabel, missingSkillsLabel);
                matchSection.setPadding(new Insets(6, 0, 6, 0));

                Label commentLabel = new Label();
                if (app.getReviewComment() != null && !app.getReviewComment().isEmpty()) {
                    commentLabel.setText("Review Comment: " + app.getReviewComment());
                    commentLabel.getStyleClass().add("muted-text");
                }

                Button viewProfileBtn = new Button("View Profile");
                viewProfileBtn.getStyleClass().add("btn-info");

                Button aiAnalysisBtn = new Button("AI Analysis");
                aiAnalysisBtn.getStyleClass().add("btn-purple");
                if (aiSkillMatchService == null) aiAnalysisBtn.setDisable(true);

                Button approveBtn = new Button("Approve");
                approveBtn.getStyleClass().add("btn-success");

                Button rejectBtn = new Button("Reject");
                rejectBtn.getStyleClass().add("btn-danger");

                if (!"Pending".equals(app.getStatus())) {
                    approveBtn.setDisable(true);
                    rejectBtn.setDisable(true);
                }

                HBox actionSpacer = new HBox();
                HBox.setHgrow(actionSpacer, Priority.ALWAYS);

                HBox actionRow = new HBox(10, viewProfileBtn, aiAnalysisBtn, actionSpacer, approveBtn, rejectBtn);
                actionRow.setAlignment(Pos.CENTER_LEFT);

                card.getChildren().addAll(headerRow, infoRow, matchSection);
                if (commentLabel.getText() != null && !commentLabel.getText().isEmpty()) {
                    card.getChildren().add(commentLabel);
                }
                card.getChildren().add(actionRow);

                appListBox.getChildren().add(card);

                viewProfileBtn.setOnAction(e -> showTAProfile(app.getTaId()));
                aiAnalysisBtn.setOnAction(e -> showAIAnalysis(applicant, selectedJob, match));
                approveBtn.setOnAction(e -> reviewApplication(app, "Approved"));
                rejectBtn.setOnAction(e -> reviewApplication(app, "Rejected"));
            }

            pageLabel.setText(String.format("Page %d / %d", currentPage, totalPages));
        } catch (Exception e) {
            Label err = new Label("Load failed: " + e.getMessage());
            err.getStyleClass().add("status-error");
            appListBox.getChildren().add(err);
            e.printStackTrace();
        }
    }

    private String statusBadgeClass(String status) {
        if ("Approved".equals(status)) return "badge-success";
        if ("Rejected".equals(status)) return "badge-danger";
        if ("Pending".equals(status))  return "badge-warning";
        return "badge-neutral";
    }

    private String recommendationBadgeClass(String level) {
        if ("Strong Match".equals(level))   return "badge-success";
        if ("Moderate Match".equals(level)) return "badge-warning";
        if ("Weak Match".equals(level))     return "badge-danger";
        return "badge-neutral";
    }

    private String formatList(List<String> list) {
        if (list == null || list.isEmpty()) return "None";
        return String.join(", ", list);
    }

    private String safe(String value) { return value == null ? "" : value; }

    private void showTAProfile(String taId) {
        try {
            var applicant = applicantService.getApplicantById(taId);
            if (applicant != null) {
                Dialog<Void> dialog = new Dialog<>();
                dialog.setTitle("TA Profile");
                dialog.setHeaderText("TA ID: " + taId);

                VBox content = new VBox();
                content.getStyleClass().add("profile-content");

                VBox profileSection = new VBox(14);
                profileSection.getStyleClass().add("profile-section");

                createStyledInfoRow(profileSection, "Name:",       applicant.getName() != null ? applicant.getName() : "N/A");
                createStyledInfoRow(profileSection, "Email:",      applicant.getEmail() != null ? applicant.getEmail() : "N/A");
                createStyledInfoRow(profileSection, "Phone:",      applicant.getPhone() != null ? applicant.getPhone() : "N/A");
                createStyledInfoRow(profileSection, "Skills:",     applicant.getSkills() != null ? applicant.getSkills() : "N/A");
                createStyledInfoRow(profileSection, "Skill Tags:", applicant.getSkillTags() != null ? applicant.getSkillTags() : "N/A");
                createStyledInfoRow(profileSection, "Courses:",    applicant.getCourses() != null ? applicant.getCourses() : "N/A");

                VBox cvSection = new VBox(14);
                cvSection.getStyleClass().add("cv-section");

                HBox cvBox = new HBox(12);
                cvBox.getStyleClass().add("cv-box");

                Label cvPathLabel = new Label(applicant.getCvPath() != null ? applicant.getCvPath() : "N/A");
                cvPathLabel.getStyleClass().add("cv-path-label");

                Button downloadBtn = new Button("Download CV");
                downloadBtn.getStyleClass().add("download-btn");

                if (applicant.getCvPath() == null || applicant.getCvPath().isEmpty()) {
                    downloadBtn.setDisable(true);
                }

                downloadBtn.setOnAction(e -> downloadCV(applicant.getCvPath()));
                cvBox.getChildren().addAll(cvPathLabel, downloadBtn);
                cvSection.getChildren().add(cvBox);

                content.getChildren().addAll(profileSection, cvSection);

                ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getDialogPane().getButtonTypes().add(closeButtonType);
                dialog.getDialogPane().getStyleClass().add("ta-profile-dialog");
                dialog.getDialogPane().setContent(content);

                java.net.URL css = getClass().getResource("/styles/app-theme.css");
                if (css != null) dialog.getDialogPane().getStylesheets().add(css.toExternalForm());

                dialog.getDialogPane().setPrefWidth(560);
                dialog.getDialogPane().setPrefHeight(460);

                dialog.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Not Found");
                alert.setContentText("TA profile not found.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAIAnalysis(Applicant applicant, Job job, SkillMatchResult match) {
        if (aiSkillMatchService == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("AI Analysis Unavailable");
            alert.setContentText("AI analysis is not available because no API key was found. Please configure the DeepSeek API key.");
            alert.showAndWait();
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("AI Skill Match Analysis");
        dialog.setHeaderText(null);

        VBox mainContent = new VBox(16);
        mainContent.setPadding(new Insets(20));
        mainContent.getStyleClass().add("app-page");

        Label titleLabel = new Label("AI Skill Match Analysis");
        titleLabel.getStyleClass().add("page-title");
        Label subtitleLabel = new Label(safe(applicant.getName()) + " → " + safe(job.getCourseName()));
        subtitleLabel.getStyleClass().add("page-subtitle");

        VBox headerBox = new VBox(4, titleLabel, subtitleLabel);
        headerBox.getStyleClass().add("hero-card");

        // Score card
        VBox scoreCard = new VBox(12);
        scoreCard.getStyleClass().add("surface-card");
        scoreCard.setAlignment(Pos.CENTER);

        Label scoreTitle = new Label("Match Overview");
        scoreTitle.getStyleClass().add("section-title");

        HBox scoreRow = new HBox(30);
        scoreRow.setAlignment(Pos.CENTER);

        VBox scoreBox = new VBox(4);
        scoreBox.setAlignment(Pos.CENTER);
        Label scoreValue = new Label(String.format("%.1f%%", match.getMatchScore()));
        scoreValue.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1f8957;");
        Label scoreLabel = new Label("Match Score");
        scoreLabel.getStyleClass().add("muted-text");
        scoreBox.getChildren().addAll(scoreValue, scoreLabel);

        VBox levelBox = new VBox(4);
        levelBox.setAlignment(Pos.CENTER);
        Label levelValue = new Label(safe(match.getRecommendationLevel()));
        levelValue.getStyleClass().addAll("badge", recommendationBadgeClass(match.getRecommendationLevel()));
        Label levelLabel = new Label("Recommendation");
        levelLabel.getStyleClass().add("muted-text");
        levelBox.getChildren().addAll(levelValue, levelLabel);

        scoreRow.getChildren().addAll(scoreBox, levelBox);

        HBox skillsRow = new HBox(28);
        skillsRow.setAlignment(Pos.CENTER);

        VBox matchedBox = new VBox(6);
        matchedBox.setAlignment(Pos.CENTER);
        Label matchedTitle = new Label("Matched Skills");
        matchedTitle.getStyleClass().add("section-title");
        Label matchedValue = new Label(formatList(match.getMatchedSkills()));
        matchedBox.getChildren().addAll(matchedTitle, matchedValue);

        VBox missingBox = new VBox(6);
        missingBox.setAlignment(Pos.CENTER);
        Label missingTitle = new Label("Missing Skills");
        missingTitle.getStyleClass().add("section-title");
        Label missingValue = new Label(formatList(match.getMissingSkills()));
        missingValue.getStyleClass().add("muted-text");
        missingBox.getChildren().addAll(missingTitle, missingValue);

        skillsRow.getChildren().addAll(matchedBox, missingBox);

        scoreCard.getChildren().addAll(scoreTitle, new Separator(), scoreRow, skillsRow);

        // Analysis card
        VBox analysisCard = new VBox(12);
        analysisCard.getStyleClass().add("surface-card");

        Label analysisTitle = new Label("AI Analysis");
        analysisTitle.getStyleClass().add("section-title");

        TextArea analysisArea = new TextArea();
        analysisArea.setEditable(false);
        analysisArea.setWrapText(true);
        analysisArea.setPrefHeight(200);
        analysisArea.setText("Analyzing... Please wait...");

        analysisCard.getChildren().addAll(analysisTitle, new Separator(), analysisArea);

        mainContent.getChildren().addAll(headerBox, scoreCard, analysisCard);

        new Thread(() -> {
            try {
                String analysis = aiSkillMatchService.analyzeSkillMatch(applicant, job, match);
                javafx.application.Platform.runLater(() -> analysisArea.setText(analysis));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                    analysisArea.setText("AI analysis failed:\n" + e.getMessage()));
                e.printStackTrace();
            }
        }).start();

        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        dialog.getDialogPane().setContent(mainContent);
        java.net.URL css = getClass().getResource("/styles/app-theme.css");
        if (css != null) dialog.getDialogPane().getStylesheets().add(css.toExternalForm());

        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(650);

        dialog.showAndWait();
    }

    private void createStyledInfoRow(VBox parent, String labelText, String value) {
        HBox row = new HBox(14);
        row.getStyleClass().add("profile-info-row");

        Label label = new Label(labelText);
        label.getStyleClass().add("profile-info-label");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("profile-info-value");

        row.getChildren().addAll(label, valueLabel);
        parent.getChildren().add(row);
    }

    private void downloadCV(String cvPath) {
        if (cvPath == null || cvPath.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No CV");
            alert.setContentText("No CV file available for download.");
            alert.showAndWait();
            return;
        }

        try {
            String fileName = cvPath;
            if (cvPath.contains("\\")) {
                fileName = cvPath.substring(cvPath.lastIndexOf("\\") + 1);
            } else if (cvPath.contains("/")) {
                fileName = cvPath.substring(cvPath.lastIndexOf("/") + 1);
            }

            String[] possiblePaths = {
                "data/resumes/" + fileName,
                "../data/resumes/" + fileName,
                "TA-Recruitment-System/data/resumes/" + fileName,
                "../TA-Recruitment-System/data/resumes/" + fileName,
                "../../data/resumes/" + fileName,
                "../../TA-Recruitment-System/data/resumes/" + fileName,
                "resumes/" + fileName,
                "../resumes/" + fileName
            };

            File cvFile = null;
            for (String path : possiblePaths) {
                File tempFile = new File(path);
                if (tempFile.exists()) { cvFile = tempFile; break; }
            }

            if (cvFile == null || !cvFile.exists()) {
                try {
                    java.net.URL resourceUrl = getClass().getResource("/resumes/" + fileName);
                    if (resourceUrl != null) cvFile = new File(resourceUrl.toURI());
                } catch (Exception ignored) { }
            }

            if (cvFile == null || !cvFile.exists()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File Not Found");
                alert.setContentText("CV file not found. Please ensure the file exists in the data/resumes directory.");
                alert.showAndWait();
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save CV File");
            fileChooser.setInitialFileName(cvFile.getName());
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
            );

            File saveFile = fileChooser.showSaveDialog(stage);
            if (saveFile != null) {
                Files.copy(cvFile.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Download Successful");
                alert.setContentText("CV file downloaded successfully to:\n" + saveFile.getAbsolutePath());
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Download Failed");
            alert.setContentText("Failed to download CV file: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void reviewApplication(Application app, String result) {
        if (!"Pending".equals(app.getStatus())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error");
            alert.setContentText("Only 'Pending' applications can be reviewed.");
            alert.showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Review Application");
        dialog.setHeaderText(result + " Application: " + app.getApplicationId());
        dialog.setContentText("Review Comment (optional, max 50 chars):");

        dialog.showAndWait().ifPresent(comment -> {
            if (comment.length() > 50) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Error");
                alert.setContentText("Comment must be 50 characters or less.");
                alert.showAndWait();
                return;
            }

            try {
                boolean success = moService.reviewApplication(app.getApplicationId(), moName, result, comment);
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setContentText("Application " + result.toLowerCase() + " successfully.");
                    alert.showAndWait();
                    currentPage = 1;
                    loadApplications(new Label());
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Failed to review application.");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Error: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        });
    }
}
