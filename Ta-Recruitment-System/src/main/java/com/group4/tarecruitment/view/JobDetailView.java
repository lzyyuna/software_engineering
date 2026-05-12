package com.group4.tarecruitment.view;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.model.Job;
import com.group4.tarecruitment.model.SkillMatchResult;
import com.group4.tarecruitment.service.AISkillMatchService;
import com.group4.tarecruitment.service.JobService;
import com.group4.tarecruitment.service.SkillMatchService;
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
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

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
        title.setFont(new Font(18));
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label courseTitle = new Label(job.getCourseName());
        courseTitle.setFont(new Font(14));
        courseTitle.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");

        VBox infoBox = new VBox(8);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        String labelStyle = "-fx-font-size: 13px; -fx-text-fill: #2c3e50;";
        infoBox.getChildren().addAll(
                createRow("Job ID: ", job.getJobId(), labelStyle),
                createRow("Course Name: ", job.getCourseName(), labelStyle),
                createRow("Position Type: ", job.getPositionType(), labelStyle),
                createRow("Weekly Workload: ", job.getWeeklyWorkload() + " hours", labelStyle),
                createRow("MO in Charge: ", job.getMoName(), labelStyle),
                createRow("Release Time: ", job.getReleaseTime(), labelStyle),
                createRow("Deadline: ", job.getDeadline(), labelStyle),
                createRow("Skill Requirements: ", job.getSkillRequirements(), labelStyle),
                createRow("Job Content: ", job.getJobContent(), labelStyle)
        );

        SkillMatchResult matchResult = skillMatchService.match(applicant, job);

        VBox matchBox = new VBox(8);
        matchBox.setPadding(new Insets(15));
        matchBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label matchTitle = new Label("Your Skill Match Analysis");
        matchTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label scoreLabel = new Label("Match Score: " + String.format("%.1f", matchResult.getMatchScore()) + "%");
        scoreLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        Label matchedLabel = new Label("Matched Skills: " + formatList(matchResult.getMatchedSkills()));
        matchedLabel.setStyle(labelStyle);

        Label missingLabel = new Label("Missing Skills: " + formatList(matchResult.getMissingSkills()));
        missingLabel.setStyle(labelStyle);

        Label levelLabel = new Label("Recommendation: " + matchResult.getRecommendationLevel());
        levelLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #8e44ad; -fx-font-weight: bold;");

        Button aiAnalyzeBtn = new Button("AI Analyze My Fit");
        aiAnalyzeBtn.setStyle("-fx-font-size: 13px; -fx-padding: 7 16; -fx-background-radius: 6; "
                + "-fx-font-weight: bold; -fx-background-color: #9b59b6; -fx-text-fill: white;");

        matchBox.getChildren().addAll(
                matchTitle,
                scoreLabel,
                matchedLabel,
                missingLabel,
                levelLabel,
                aiAnalyzeBtn
        );

        Button applyBtn = new Button("Apply for This Position");
        Button backBtn = new Button("Back to Job List");
        Button backToHomeBtn = new Button("Back to TA Home");

        String btnStyle = "-fx-font-size: 13px; -fx-padding: 7 16; -fx-background-radius: 6; -fx-font-weight: bold;";
        applyBtn.setStyle(btnStyle + "-fx-background-color: #27ae60; -fx-text-fill: white;");
        backBtn.setStyle(btnStyle + "-fx-background-color: #3498db; -fx-text-fill: white;");
        backToHomeBtn.setStyle(btnStyle + "-fx-background-color: #95a5a6; -fx-text-fill: white;");

        Label resultLabel = new Label("");
        resultLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 5 0 0 0;");

        applyBtn.setOnAction(e -> {
            try {
                String appId = jobService.submitApplication(applicant.getTaId(), job.getJobId());
                if (appId == null) {
                    resultLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px; -fx-font-weight: bold;");
                    resultLabel.setText("❌ You have already applied for this position.");
                } else {
                    resultLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px; -fx-font-weight: bold;");
                    resultLabel.setText("✅ Application submitted successfully. Application ID: " + appId);
                    applyBtn.setDisable(true);
                }
            } catch (Exception ex) {
                resultLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px; -fx-font-weight: bold;");
                resultLabel.setText("❌ Application failed: " + ex.getMessage());
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

        HBox btnBox = new HBox(15, applyBtn, backBtn, backToHomeBtn);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        VBox root = new VBox(12, title, courseTitle, infoBox, matchBox, btnBox, resultLabel);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f6fa;");
        root.setAlignment(Pos.TOP_LEFT);

        return root;
    }

    private void runAiAnalysis(SkillMatchResult matchResult) {
        String apiKey = AISkillMatchService.getApiKeyFromEnv();

        if (apiKey == null || apiKey.isBlank()) {
            TextInputDialog keyDialog = new TextInputDialog();
            keyDialog.setTitle("API Key Required");
            keyDialog.setHeaderText("Enter your DeepSeek API Key");
            keyDialog.setContentText("API Key:");
            var result = keyDialog.showAndWait();
            if (result.isEmpty() || result.get().isBlank()) {
                return;
            }
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
        loadingStage.setScene(new Scene(loadingBox, 320, 160));
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

        HBox header = new HBox(10);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #6a1b9a;");

        Label headerTitle = new Label("AI Skill Match Analysis");
        headerTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");
        header.getChildren().add(headerTitle);

        SkillMatchResult matchResult = skillMatchService.match(applicant, job);
        Map<String, String> sections = parseAiSections(analysisResult);

        VBox content = new VBox(10);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: #f5f6fa;");

        Label contextLabel = new Label("Course: " + safe(job.getCourseName())
                + "    Match Score: " + String.format("%.1f", matchResult.getMatchScore()) + "%");
        contextLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label recommendationBadge = new Label("Recommendation: " + safe(matchResult.getRecommendationLevel()));
        recommendationBadge.setStyle(getRecommendationBadgeStyle(matchResult.getRecommendationLevel()));

        HBox summaryRow = new HBox(12, contextLabel, recommendationBadge);
        summaryRow.setAlignment(Pos.CENTER_LEFT);

        VBox cardsBox = new VBox(12);
        if (sections.isEmpty()) {
            cardsBox.getChildren().add(buildSectionCard("Analysis", analysisResult, "#37474f"));
        } else {
            addSectionCard(cardsBox, sections, "OVERALL FIT", "Overall Fit", "#1565c0");
            addSectionCard(cardsBox, sections, "MATCHED STRENGTHS", "Matched Strengths", "#1b5e20");
            addSectionCard(cardsBox, sections, "MISSING SKILLS", "Missing Skills", "#e65100");
            addSectionCard(cardsBox, sections, "RECOMMENDATION", "Recommendation", "#6a1b9a");
        }

        content.getChildren().addAll(summaryRow, cardsBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f6fa; -fx-background: #f5f6fa;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        HBox footer = new HBox(10);
        footer.setPadding(new Insets(10, 16, 10, 16));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: #eceff1; -fx-border-color: #cfd8dc; -fx-border-width: 1 0 0 0;");

        Button copyBtn = new Button("Copy to Clipboard");
        copyBtn.setStyle("-fx-background-color: #8e24aa; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 7 14;");
        copyBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent contentToCopy = new ClipboardContent();
            contentToCopy.putString(analysisResult == null ? "" : analysisResult);
            clipboard.setContent(contentToCopy);
            copyBtn.setText("Copied!");
        });

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #546e7a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 7 20;");
        closeBtn.setOnAction(e -> resultStage.close());
        footer.getChildren().addAll(copyBtn, closeBtn);

        VBox root = new VBox(header, scrollPane, footer);
        root.setStyle("-fx-background-color: #f5f6fa;");

        Scene scene = new Scene(root, 680, 520);
        resultStage.setScene(scene);
        resultStage.setMinWidth(480);
        resultStage.setMinHeight(360);
        resultStage.show();
    }

    private void addSectionCard(VBox cardsBox, Map<String, String> sections,
                                String key, String title, String accentColor) {
        String content = sections.get(key);
        if (content != null && !content.isBlank()) {
            cardsBox.getChildren().add(buildSectionCard(title, content, accentColor));
        }
    }

    private VBox buildSectionCard(String title, String body, String accentColor) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 6;"
                + "-fx-border-color: " + accentColor + "; -fx-border-width: 0 0 0 5;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 6, 0, 0, 2);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

        Label bodyLabel = new Label(safe(body));
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #263238; -fx-line-spacing: 3;");

        card.getChildren().addAll(titleLabel, bodyLabel);
        return card;
    }

    private Map<String, String> parseAiSections(String analysisResult) {
        Map<String, String> sections = new LinkedHashMap<>();
        if (analysisResult == null || analysisResult.isBlank()) {
            return sections;
        }

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
                if (currentValue.length() > 0) {
                    currentValue.append("\n");
                }
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
        if (upper.equals("MATCHED STRENGTHS:") || upper.startsWith("MATCHED STRENGTHS:")) return "MATCHED STRENGTHS";
        if (upper.equals("MISSING SKILLS:") || upper.startsWith("MISSING SKILLS:")) return "MISSING SKILLS";
        if (upper.startsWith("RECOMMENDATION:")) return "RECOMMENDATION";
        return null;
    }

    private String getRecommendationBadgeStyle(String recommendationLevel) {
        String level = recommendationLevel == null ? "" : recommendationLevel;
        String backgroundColor;
        String textColor;

        switch (level) {
            case "Strong Match":
                backgroundColor = "#e8f5e9";
                textColor = "#1b5e20";
                break;
            case "Moderate Match":
                backgroundColor = "#fff8e1";
                textColor = "#e65100";
                break;
            case "Weak Match":
                backgroundColor = "#fdecea";
                textColor = "#c62828";
                break;
            default:
                backgroundColor = "#eceff1";
                textColor = "#455a64";
                break;
        }

        return "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: " + textColor + ";"
                + "-fx-background-color: " + backgroundColor + ";"
                + "-fx-background-radius: 4;"
                + "-fx-padding: 5 10;";
    }

    private String formatList(java.util.List<String> list) {
        if (list == null || list.isEmpty()) {
            return "None";
        }
        return String.join(", ", list);
    }

    private HBox createRow(String label, String value, String style) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);

        Label l1 = new Label(label);
        l1.setStyle(style + "-fx-font-weight: bold;");

        Label l2 = new Label(value == null ? "" : value);
        l2.setStyle(style);

        row.getChildren().addAll(l1, l2);
        return row;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
