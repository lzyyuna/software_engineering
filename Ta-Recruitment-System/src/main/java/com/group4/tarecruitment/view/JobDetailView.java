package com.group4.tarecruitment.view;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.model.Job;
import com.group4.tarecruitment.model.SkillMatchResult;
import com.group4.tarecruitment.service.AISkillMatchService;
import com.group4.tarecruitment.service.JobService;
import com.group4.tarecruitment.service.SkillMatchService;
import com.group4.tarecruitment.util.ThemeManager;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds the TA-facing job detail page, including skill matching and application submission.
 */
public class JobDetailView {
    private final Stage stage;
    private final Applicant applicant;
    private final Job job;
    private final JobService jobService = new JobService();
    private final SkillMatchService skillMatchService = new SkillMatchService();

    public JobDetailView(Stage stage, Applicant applicant, Job job) {
        this.stage = stage;
        this.applicant = applicant;
        this.job = job;
    }

    public Parent createContent() {
        Label title = new Label("Job Details");
        title.getStyleClass().add("page-title");

        Label courseTitle = new Label(safe(job.getCourseName()));
        courseTitle.getStyleClass().add("section-title");

        VBox infoBox = new VBox(8);
        infoBox.getStyleClass().add("surface-card");
        infoBox.getChildren().addAll(
                createRow("Job ID:",            job.getJobId()),
                createRow("Course Name:",       job.getCourseName()),
                createRow("Position Type:",     job.getPositionType()),
                createRow("Weekly Workload:",   job.getWeeklyWorkload() + " hours"),
                createRow("MO in Charge:",      job.getMoName()),
                createRow("Release Time:",      job.getReleaseTime()),
                createRow("Deadline:",          job.getDeadline()),
                createRow("Skill Requirements:",job.getSkillRequirements()),
                createRow("Job Content:",       job.getJobContent())
        );

        SkillMatchResult matchResult = skillMatchService.match(applicant, job);

        Label matchTitle = new Label("Your Skill Match Analysis");
        matchTitle.getStyleClass().add("section-title");

        Label scoreLabel = new Label("Match: " + String.format("%.1f", matchResult.getMatchScore()) + "%");
        scoreLabel.getStyleClass().addAll("badge", "badge-success");

        Label matchedLabel = new Label("Matched Skills: " + formatList(matchResult.getMatchedSkills()));
        Label missingLabel = new Label("Missing Skills: " + formatList(matchResult.getMissingSkills()));
        missingLabel.getStyleClass().add("muted-text");

        Label levelLabel = new Label(safe(matchResult.getRecommendationLevel()));
        levelLabel.getStyleClass().addAll("badge", recommendationBadgeClass(matchResult.getRecommendationLevel()));

        Button aiAnalyzeBtn = new Button("AI Analyze My Fit");
        aiAnalyzeBtn.getStyleClass().add("btn-purple");

        HBox badgeRow = new HBox(12, scoreLabel, levelLabel);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        VBox matchBox = new VBox(10, matchTitle, badgeRow, matchedLabel, missingLabel, aiAnalyzeBtn);
        matchBox.getStyleClass().add("surface-card");

        Button applyBtn = new Button("Apply for This Position");
        Button backBtn = new Button("Back to Job List");
        Button backToHomeBtn = new Button("Back to TA Home");

        applyBtn.getStyleClass().add("btn-success");
        backBtn.getStyleClass().add("btn-info");
        backToHomeBtn.getStyleClass().add("btn-muted");

        Label resultLabel = new Label("");
        resultLabel.getStyleClass().add("status-label");

        initializeApplyState(applyBtn, resultLabel);

        applyBtn.setOnAction(e -> {
            try {
                String appId = jobService.submitApplication(applicant.getTaId(), job.getJobId());
                resultLabel.getStyleClass().removeAll("status-error", "status-success");
                if (appId == null) {
                    resultLabel.getStyleClass().add("status-error");
                    resultLabel.setText("You have already applied for this position.");
                } else {
                    resultLabel.getStyleClass().add("status-success");
                    resultLabel.setText("Application submitted successfully. Application ID: " + appId);
                    applyBtn.setDisable(true);
                }
            } catch (Exception ex) {
                resultLabel.getStyleClass().removeAll("status-error", "status-success");
                resultLabel.getStyleClass().add("status-error");
                resultLabel.setText("Application failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        aiAnalyzeBtn.setOnAction(e -> runAiAnalysis(matchResult));

        backBtn.setOnAction(e -> {
            JobListView jobListView = new JobListView(stage, applicant);
            stage.getScene().setRoot(jobListView.createContent());
            stage.setTitle("Available TA Positions");
        });

        backToHomeBtn.setOnAction(e -> {
            TAHomeView taHomeView = new TAHomeView(stage, applicant);
            stage.getScene().setRoot(taHomeView.createContent());
            stage.setTitle("TA Dashboard");
        });

        HBox btnBox = new HBox(12, applyBtn, backBtn, backToHomeBtn);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        btnBox.setPadding(new Insets(8, 0, 0, 0));

        VBox root = new VBox(14, title, courseTitle, infoBox, matchBox, btnBox, resultLabel);
        root.getStyleClass().add("app-page");
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.TOP_LEFT);

        return root;
    }

    private void initializeApplyState(Button applyBtn, Label resultLabel) {
        try {
            if (jobService.hasApplied(applicant.getTaId(), job.getJobId())) {
                applyBtn.setDisable(true);
                resultLabel.getStyleClass().add("status-error");
                resultLabel.setText("You have already applied for this position.");
            }
        } catch (Exception ex) {
            resultLabel.getStyleClass().add("status-error");
            resultLabel.setText("Could not check application status: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void runAiAnalysis(SkillMatchResult matchResult) {
        String apiKey = AISkillMatchService.getApiKeyFromEnv();

        if (apiKey == null || apiKey.isBlank()) {
            TextInputDialog keyDialog = new TextInputDialog();
            keyDialog.setTitle("API Key Required");
            keyDialog.setHeaderText("Enter your DeepSeek API Key");
            keyDialog.setContentText("API Key:");
            var result = keyDialog.showAndWait();
            if (result.isEmpty() || result.get().isBlank()) return;
            apiKey = result.get().trim();
        }

        final String finalApiKey = apiKey;

        Stage loadingStage = new Stage();
        loadingStage.initOwner(stage);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle("AI Skill Match Analysis");

        ProgressIndicator spinner = new ProgressIndicator();
        Label loadingLabel = new Label("Analyzing your fit with AI...");
        VBox loadingBox = new VBox(15, spinner, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(30));
        loadingStage.setScene(ThemeManager.createScene(loadingBox, 320, 160));
        loadingStage.show();

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                AISkillMatchService aiService = new AISkillMatchService(finalApiKey);
                return aiService.analyzeSkillMatch(applicant, job, matchResult);
            }
        };

        task.setOnSucceeded(ev -> {
            loadingStage.close();
            showAiResultDialog(task.getValue());
        });

        task.setOnFailed(ev -> {
            loadingStage.close();
            Throwable ex = task.getException();
            showAiResultDialog("AI analysis failed: "
                    + (ex != null && ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void showAiResultDialog(String analysisResult) {
        Stage resultStage = new Stage();
        resultStage.initModality(Modality.APPLICATION_MODAL);
        resultStage.initOwner(stage);
        resultStage.setTitle("AI Skill Match Analysis");

        Label headerTitle = new Label("AI Skill Match Analysis");
        headerTitle.getStyleClass().add("page-title");

        SkillMatchResult matchResult = skillMatchService.match(applicant, job);
        Map<String, String> sections = parseAiSections(analysisResult);

        Label contextLabel = new Label("Course: " + safe(job.getCourseName())
                + "   ·   Match: " + String.format("%.1f", matchResult.getMatchScore()) + "%");
        contextLabel.getStyleClass().add("muted-text");

        Label recommendationBadge = new Label(safe(matchResult.getRecommendationLevel()));
        recommendationBadge.getStyleClass().addAll("badge", recommendationBadgeClass(matchResult.getRecommendationLevel()));

        HBox summaryRow = new HBox(12, contextLabel, recommendationBadge);
        summaryRow.setAlignment(Pos.CENTER_LEFT);

        VBox cardsBox = new VBox(12);
        if (sections.isEmpty()) {
            cardsBox.getChildren().add(buildSectionCard("Analysis", analysisResult));
        } else {
            addSectionCard(cardsBox, sections, "OVERALL FIT", "Overall Fit");
            addSectionCard(cardsBox, sections, "MATCHED STRENGTHS", "Matched Strengths");
            addSectionCard(cardsBox, sections, "MISSING SKILLS", "Missing Skills");
            addSectionCard(cardsBox, sections, "RECOMMENDATION", "Recommendation");
        }

        VBox content = new VBox(14, headerTitle, summaryRow, cardsBox);
        content.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button copyBtn = new Button("Copy to Clipboard");
        copyBtn.getStyleClass().add("btn-purple");
        copyBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent contentToCopy = new ClipboardContent();
            contentToCopy.putString(analysisResult == null ? "" : analysisResult);
            clipboard.setContent(contentToCopy);
            copyBtn.setText("Copied!");
        });

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("btn-muted");
        closeBtn.setOnAction(e -> resultStage.close());

        HBox footer = new HBox(10, copyBtn, closeBtn);
        footer.setPadding(new Insets(12, 20, 12, 20));
        footer.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(scrollPane, footer);
        root.getStyleClass().add("app-page");

        Scene scene = ThemeManager.createScene(root, 680, 520);
        resultStage.setScene(scene);
        resultStage.setMinWidth(480);
        resultStage.setMinHeight(360);
        resultStage.show();
    }

    private void addSectionCard(VBox cardsBox, Map<String, String> sections,
                                String key, String title) {
        String content = sections.get(key);
        if (content != null && !content.isBlank()) {
            cardsBox.getChildren().add(buildSectionCard(title, content));
        }
    }

    private VBox buildSectionCard(String title, String body) {
        VBox card = new VBox(6);
        card.getStyleClass().add("surface-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label bodyLabel = new Label(safe(body));
        bodyLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, bodyLabel);
        return card;
    }

    private Map<String, String> parseAiSections(String analysisResult) {
        Map<String, String> sections = new LinkedHashMap<>();
        if (analysisResult == null || analysisResult.isBlank()) return sections;

        String currentKey = null;
        StringBuilder currentValue = new StringBuilder();
        for (String rawLine : analysisResult.split("\\R")) {
            String line = rawLine.trim();
            String detectedKey = detectSectionKey(line);

            if (detectedKey != null) {
                if (currentKey != null) {
                    sections.put(currentKey, currentValue.toString().trim());
                }
                currentKey = detectedKey;
                currentValue.setLength(0);

                int colonIndex = line.indexOf(':');
                if (colonIndex >= 0 && colonIndex + 1 < line.length()) {
                    currentValue.append(line.substring(colonIndex + 1).trim());
                }
            } else if (currentKey != null) {
                if (currentValue.length() > 0) currentValue.append("\n");
                currentValue.append(rawLine.strip());
            }
        }

        if (currentKey != null) {
            sections.put(currentKey, currentValue.toString().trim());
        }
        return sections;
    }

    private String detectSectionKey(String line) {
        String upper = line.toUpperCase();
        if (upper.startsWith("OVERALL FIT:")) return "OVERALL FIT";
        if (upper.startsWith("MATCHED STRENGTHS:")) return "MATCHED STRENGTHS";
        if (upper.startsWith("MISSING SKILLS:")) return "MISSING SKILLS";
        if (upper.startsWith("RECOMMENDATION:")) return "RECOMMENDATION";
        return null;
    }

    private String recommendationBadgeClass(String level) {
        if ("Strong Match".equals(level))   return "badge-success";
        if ("Moderate Match".equals(level)) return "badge-warning";
        if ("Weak Match".equals(level))     return "badge-danger";
        return "badge-neutral";
    }

    private String formatList(java.util.List<String> list) {
        if (list == null || list.isEmpty()) return "None";
        return String.join(", ", list);
    }

    private HBox createRow(String label, String value) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);

        Label l1 = new Label(label);
        l1.getStyleClass().add("profile-info-label");

        Label l2 = new Label(value == null ? "" : value);
        l2.setWrapText(true);

        row.getChildren().addAll(l1, l2);
        return row;
    }

    private String safe(String value) { return value == null ? "" : value; }
}
