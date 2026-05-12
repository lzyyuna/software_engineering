package com.group4.tarecruitment.view;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.model.Job;
import com.group4.tarecruitment.model.SkillMatchResult;
import com.group4.tarecruitment.service.JobService;
import com.group4.tarecruitment.service.SkillMatchService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class JobListView {
    private final Stage stage;
    private final Applicant applicant;
    private final JobService jobService = new JobService();
    private final SkillMatchService skillMatchService = new SkillMatchService();

    private final int PAGE_SIZE = 3;
    private int currentPage = 1;

    // 当前页使用的过滤控件
    private CheckBox javaCb;
    private CheckBox englishCb;
    private CheckBox teachingCb;
    private CheckBox pythonCb;
    private CheckBox officeCb;
    private CheckBox strongOnlyCb;
    private ComboBox<String> typeCombo;

    public JobListView(Stage stage, Applicant applicant) {
        this.stage = stage;
        this.applicant = applicant;
    }

    public Parent createContent() {
        Label title = new Label("Available TA Positions");
        title.setFont(new Font(18));
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button refreshBtn = new Button("Refresh List");
        Button myAppsBtn = new Button("My Applications");
        Button backToHomeBtn = new Button("Back to TA Home");

        String btnStyle = "-fx-font-size: 14px; -fx-padding: 7 14; -fx-background-radius: 5; -fx-font-weight: bold;";
        refreshBtn.setStyle(btnStyle + "-fx-background-color: #3498db; -fx-text-fill: white;");
        myAppsBtn.setStyle(btnStyle + "-fx-background-color: #9b59b6; -fx-text-fill: white;");
        backToHomeBtn.setStyle(btnStyle + "-fx-background-color: #2ecc71; -fx-text-fill: white;");

        HBox topBar = new HBox(10, refreshBtn, myAppsBtn, backToHomeBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // ===================== Filter Bar =====================
        javaCb = new CheckBox("Java");
        englishCb = new CheckBox("English");
        teachingCb = new CheckBox("Teaching");
        pythonCb = new CheckBox("Python");
        officeCb = new CheckBox("Office");
        strongOnlyCb = new CheckBox("Only show strong matches");

        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("All", "Module TA", "Invigilation TA");
        typeCombo.setValue("All");
        typeCombo.setPrefWidth(150);

        Button resetBtn = new Button("Reset");
        resetBtn.setStyle(btnStyle + "-fx-background-color: #95a5a6; -fx-text-fill: white;");

        HBox filterSpacer = new HBox();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        HBox typeGap = new HBox();
        typeGap.setMinWidth(24);

        HBox filterTopRow = new HBox(
                10,
                new Label("Skill Tags:"),
                javaCb, englishCb, teachingCb, pythonCb, officeCb,
                typeGap,
                new Label("Position Type:"),
                typeCombo,
                filterSpacer,
                resetBtn
        );
        filterTopRow.setAlignment(Pos.CENTER_LEFT);

        HBox filterSecondRow = new HBox(10, strongOnlyCb);
        filterSecondRow.setAlignment(Pos.CENTER_LEFT);
        filterSecondRow.setPadding(new Insets(0, 0, 0, 92));

        VBox filterBar = new VBox(4, filterTopRow, filterSecondRow);
        filterBar.setPadding(new Insets(2, 0, 4, 0));

        VBox jobListBox = new VBox(8);
        jobListBox.setPadding(new Insets(10));
        jobListBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        HBox pageBox = new HBox(10);
        Button prevBtn = new Button("Previous");
        Button nextBtn = new Button("Next");
        Label pageLabel = new Label("Page 1");
        pageBox.getChildren().addAll(prevBtn, pageLabel, nextBtn);
        pageBox.setAlignment(Pos.CENTER);

        loadJobs(jobListBox, pageLabel);

        refreshBtn.setOnAction(e -> {
            currentPage = 1;
            loadJobs(jobListBox, pageLabel);
        });

        myAppsBtn.setOnAction(e -> {
            MyApplicationView myAppView = new MyApplicationView(stage, applicant);
            stage.getScene().setRoot(myAppView.createContent());
            stage.setTitle("My Applications");
        });

        backToHomeBtn.setOnAction(e -> {
            TAHomeView taHomeView = new TAHomeView(stage, applicant);
            stage.getScene().setRoot(taHomeView.createContent());
            stage.setTitle("TA Dashboard");
        });

        prevBtn.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadJobs(jobListBox, pageLabel);
            }
        });

        nextBtn.setOnAction(e -> {
            try {
                int totalPages = getTotalPages();
                if (currentPage < totalPages) {
                    currentPage++;
                    loadJobs(jobListBox, pageLabel);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 实时刷新
        javaCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        englishCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        teachingCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        pythonCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        officeCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        strongOnlyCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        typeCombo.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));

        // Reset
        resetBtn.setOnAction(e -> {
            javaCb.setSelected(false);
            englishCb.setSelected(false);
            teachingCb.setSelected(false);
            pythonCb.setSelected(false);
            officeCb.setSelected(false);
            strongOnlyCb.setSelected(false);
            typeCombo.setValue("All");
            currentPage = 1;
            loadJobs(jobListBox, pageLabel);
        });

        VBox root = new VBox(12, title, topBar, filterBar, jobListBox, new Separator(), pageBox);
        root.setPadding(new Insets(20, 25, 18, 25));
        root.setStyle("-fx-background-color: #f5f6fa;");
        return root;
    }

    private void refreshFilteredJobs(VBox jobListBox, Label pageLabel) {
        currentPage = 1;
        loadJobs(jobListBox, pageLabel);
    }

    private int getTotalPages() throws Exception {
        List<Job> filteredJobs = getFilteredAndSortedJobs();
        if (filteredJobs.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) filteredJobs.size() / PAGE_SIZE);
    }

    private List<Job> getFilteredAndSortedJobs() throws Exception {
        List<Job> activeJobs = jobService.getActiveJobs();
        List<Job> filteredJobs = new ArrayList<>();

        for (Job job : activeJobs) {
            if (matchesFilters(job)) {
                filteredJobs.add(job);
            }
        }

        // 按匹配度从高到低排序
        filteredJobs.sort((j1, j2) -> {
            double s1 = skillMatchService.match(applicant, j1).getMatchScore();
            double s2 = skillMatchService.match(applicant, j2).getMatchScore();
            return Double.compare(s2, s1);
        });

        return filteredJobs;
    }

    private boolean matchesFilters(Job job) {
        String skillRequirements = job.getSkillRequirements() == null
                ? ""
                : job.getSkillRequirements().toLowerCase();

        // 多选技能标签：叠加过滤（AND）
        if (javaCb.isSelected() && !skillRequirements.contains("java")) {
            return false;
        }
        if (englishCb.isSelected() && !skillRequirements.contains("english")) {
            return false;
        }
        if (teachingCb.isSelected() && !skillRequirements.contains("teaching")) {
            return false;
        }
        if (pythonCb.isSelected() && !skillRequirements.contains("python")) {
            return false;
        }
        if (officeCb.isSelected() && !skillRequirements.contains("office")) {
            return false;
        }

        // 单选岗位类型
        if (strongOnlyCb.isSelected()) {
            SkillMatchResult match = skillMatchService.match(applicant, job);
            if (!"Strong Match".equals(match.getRecommendationLevel())) {
                return false;
            }
        }

        String selectedType = typeCombo.getValue();
        if (selectedType != null
                && !"All".equals(selectedType)
                && job.getPositionType() != null
                && !job.getPositionType().equalsIgnoreCase(selectedType)) {
            return false;
        }

        return selectedType == null
                || "All".equals(selectedType)
                || (job.getPositionType() != null && job.getPositionType().equalsIgnoreCase(selectedType));
    }

    private void loadJobs(VBox jobListBox, Label pageLabel) {
        jobListBox.getChildren().clear();
        try {
            List<Job> filteredJobs = getFilteredAndSortedJobs();
            int totalPages = filteredJobs.isEmpty() ? 1 : (int) Math.ceil((double) filteredJobs.size() / PAGE_SIZE);

            if (currentPage > totalPages) {
                currentPage = totalPages;
            }
            if (currentPage < 1) {
                currentPage = 1;
            }

            int start = (currentPage - 1) * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, filteredJobs.size());

            List<Job> pageJobs = filteredJobs.isEmpty() ? new ArrayList<>() : filteredJobs.subList(start, end);

            if (pageJobs.isEmpty()) {
                Label emptyLabel = new Label("No Matched Jobs");
                emptyLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #7f8c8d;");
                jobListBox.getChildren().add(emptyLabel);
                pageLabel.setText("Page 1 / 1");
                return;
            }

            for (Job job : pageJobs) {
                SkillMatchResult match = skillMatchService.match(applicant, job);

                VBox jobItem = new VBox(5);
                jobItem.setPadding(new Insets(8, 10, 8, 10));
                jobItem.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4,0,0,1);");

                Label courseLabel = new Label(job.getCourseName() == null ? "Untitled Course" : job.getCourseName());
                courseLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                Label basicInfoLabel = new Label(
                        "ID: " + safe(job.getJobId())
                                + "   |   Type: " + safe(job.getPositionType())
                                + "   |   Weekly Workload: " + job.getWeeklyWorkload() + "h"
                                + "   |   MO: " + safe(job.getMoName())
                );
                basicInfoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

                Label matchScoreLabel = new Label("Match Score: " + String.format("%.1f", match.getMatchScore()) + "%");
                matchScoreLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

                Label matchedSkillsLabel = new Label("Matched Skills: " + formatList(match.getMatchedSkills()));
                matchedSkillsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");

                Label missingSkillsLabel = new Label("Missing Skills: " + formatList(match.getMissingSkills()));
                missingSkillsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

                Label recommendationLabel = new Label("Recommendation: " + safe(match.getRecommendationLevel()));
                recommendationLabel.setStyle(getRecommendationBadgeStyle(match.getRecommendationLevel()));

                Button detailBtn = new Button("View Details");
                detailBtn.setStyle("-fx-font-size: 13px; -fx-padding: 6 12; -fx-background-color: #3498db; "
                        + "-fx-text-fill: white; -fx-background-radius: 5;");

                detailBtn.setOnAction(e -> {
                    JobDetailView detailView = new JobDetailView(stage, applicant, job);
                    stage.getScene().setRoot(detailView.createContent());
                    stage.setTitle("Job Details");
                });

                HBox matchInfoRow = new HBox(20, matchScoreLabel, matchedSkillsLabel, missingSkillsLabel);
                matchInfoRow.setAlignment(Pos.CENTER_LEFT);
                matchInfoRow.setPadding(new Insets(1, 0, 1, 0));

                HBox bottomBar = new HBox();
                bottomBar.setAlignment(Pos.CENTER_LEFT);
                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                bottomBar.getChildren().addAll(recommendationLabel, spacer, detailBtn);

                jobItem.getChildren().addAll(
                        courseLabel,
                        basicInfoLabel,
                        matchInfoRow,
                        bottomBar
                );

                jobListBox.getChildren().add(jobItem);
            }

            pageLabel.setText(String.format("Page %d / %d", currentPage, totalPages));
        } catch (Exception e) {
            jobListBox.getChildren().add(new Label("Load failed: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private String formatList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "None";
        }
        return String.join(", ", list);
    }

    private String safe(String value) {
        return value == null ? "" : value;
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
                + "-fx-padding: 4 10;";
    }
}
