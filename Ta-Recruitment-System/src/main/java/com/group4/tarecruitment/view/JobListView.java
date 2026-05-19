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
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the TA-facing list of recruiting jobs with filters, pagination, and skill matching.
 */
public class JobListView {
    private final Stage stage;
    private final Applicant applicant;
    private final JobService jobService = new JobService();
    private final SkillMatchService skillMatchService = new SkillMatchService();

    private final int PAGE_SIZE = 3;
    private int currentPage = 1;
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
        title.getStyleClass().add("page-title");

        Button refreshBtn = new Button("Refresh List");
        Button myAppsBtn = new Button("My Applications");
        Button backToHomeBtn = new Button("Back to TA Home");

        refreshBtn.getStyleClass().add("btn-primary");
        myAppsBtn.getStyleClass().add("btn-purple");
        backToHomeBtn.getStyleClass().add("btn-muted");

        HBox topBar = new HBox(10, refreshBtn, myAppsBtn, backToHomeBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("toolbar");

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
        resetBtn.getStyleClass().add("btn-muted");

        HBox filterSpacer = new HBox();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        HBox typeGap = new HBox();
        typeGap.setMinWidth(24);

        Label skillTagLabel = new Label("Skill Tags:");
        Label positionTypeLabel = new Label("Position Type:");

        HBox filterTopRow = new HBox(
                10,
                skillTagLabel,
                javaCb, englishCb, teachingCb, pythonCb, officeCb,
                typeGap,
                positionTypeLabel,
                typeCombo,
                filterSpacer,
                resetBtn
        );
        filterTopRow.setAlignment(Pos.CENTER_LEFT);

        HBox filterSecondRow = new HBox(10, strongOnlyCb);
        filterSecondRow.setAlignment(Pos.CENTER_LEFT);
        filterSecondRow.setPadding(new Insets(0, 0, 0, 92));

        VBox filterBar = new VBox(6, filterTopRow, filterSecondRow);
        filterBar.getStyleClass().add("filter-bar");

        VBox jobListBox = new VBox(12);
        jobListBox.getStyleClass().add("list-container");

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
        javaCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        englishCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        teachingCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        pythonCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        officeCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        strongOnlyCb.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
        typeCombo.setOnAction(e -> refreshFilteredJobs(jobListBox, pageLabel));
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

        VBox root = new VBox(14, title, topBar, filterBar, jobListBox, new Separator(), pageBox);
        root.getStyleClass().add("app-page");
        root.setPadding(new Insets(24));
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
        if (javaCb.isSelected() && !skillRequirements.contains("java")) return false;
        if (englishCb.isSelected() && !skillRequirements.contains("english")) return false;
        if (teachingCb.isSelected() && !skillRequirements.contains("teaching")) return false;
        if (pythonCb.isSelected() && !skillRequirements.contains("python")) return false;
        if (officeCb.isSelected() && !skillRequirements.contains("office")) return false;
        if (strongOnlyCb.isSelected()) {
            SkillMatchResult match = skillMatchService.match(applicant, job);
            if (!"Strong Match".equals(match.getRecommendationLevel())) {
                return false;
            }
        }

        String selectedType = typeCombo.getValue();
        return selectedType == null
                || "All".equals(selectedType)
                || (job.getPositionType() != null && job.getPositionType().equalsIgnoreCase(selectedType));
    }

    private void loadJobs(VBox jobListBox, Label pageLabel) {
        jobListBox.getChildren().clear();
        try {
            List<Job> filteredJobs = getFilteredAndSortedJobs();
            int totalPages = filteredJobs.isEmpty() ? 1 : (int) Math.ceil((double) filteredJobs.size() / PAGE_SIZE);

            if (currentPage > totalPages) currentPage = totalPages;
            if (currentPage < 1) currentPage = 1;

            int start = (currentPage - 1) * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, filteredJobs.size());

            List<Job> pageJobs = filteredJobs.isEmpty() ? new ArrayList<>() : filteredJobs.subList(start, end);

            if (pageJobs.isEmpty()) {
                Label emptyLabel = new Label("No Matched Jobs");
                emptyLabel.getStyleClass().add("empty-text");
                jobListBox.getChildren().add(emptyLabel);
                pageLabel.setText("Page 1 / 1");
                return;
            }

            for (Job job : pageJobs) {
                SkillMatchResult match = skillMatchService.match(applicant, job);

                VBox jobItem = new VBox(8);
                jobItem.getStyleClass().add("list-item-card");

                Label courseLabel = new Label(job.getCourseName() == null ? "Untitled Course" : job.getCourseName());
                courseLabel.getStyleClass().add("section-title");

                Label basicInfoLabel = new Label(
                        "ID: " + safe(job.getJobId())
                                + "   ·   Type: " + safe(job.getPositionType())
                                + "   ·   Weekly Workload: " + job.getWeeklyWorkload() + "h"
                                + "   ·   MO: " + safe(job.getMoName())
                );
                basicInfoLabel.getStyleClass().add("muted-text");

                Label matchScoreLabel = new Label("Match: " + String.format("%.1f", match.getMatchScore()) + "%");
                matchScoreLabel.getStyleClass().addAll("badge", "badge-success");

                Label matchedSkillsLabel = new Label("Matched: " + formatList(match.getMatchedSkills()));
                Label missingSkillsLabel = new Label("Missing: " + formatList(match.getMissingSkills()));
                missingSkillsLabel.getStyleClass().add("muted-text");

                Label recommendationLabel = new Label(safe(match.getRecommendationLevel()));
                recommendationLabel.getStyleClass().addAll("badge", recommendationBadgeClass(match.getRecommendationLevel()));

                Button detailBtn = new Button("View Details");
                detailBtn.getStyleClass().add("btn-info");
                detailBtn.setOnAction(e -> {
                    JobDetailView detailView = new JobDetailView(stage, applicant, job);
                    stage.getScene().setRoot(detailView.createContent());
                    stage.setTitle("Job Details");
                });

                HBox matchInfoRow = new HBox(16, matchScoreLabel, matchedSkillsLabel, missingSkillsLabel);
                matchInfoRow.setAlignment(Pos.CENTER_LEFT);

                HBox bottomBar = new HBox();
                bottomBar.setAlignment(Pos.CENTER_LEFT);
                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                bottomBar.getChildren().addAll(recommendationLabel, spacer, detailBtn);

                jobItem.getChildren().addAll(courseLabel, basicInfoLabel, matchInfoRow, bottomBar);
                jobListBox.getChildren().add(jobItem);
            }

            pageLabel.setText(String.format("Page %d / %d", currentPage, totalPages));
        } catch (Exception e) {
            Label err = new Label("Load failed: " + e.getMessage());
            err.getStyleClass().add("status-error");
            jobListBox.getChildren().add(err);
            e.printStackTrace();
        }
    }

    private String formatList(List<String> list) {
        if (list == null || list.isEmpty()) return "None";
        return String.join(", ", list);
    }

    private String safe(String value) { return value == null ? "" : value; }

    private String recommendationBadgeClass(String level) {
        if ("Strong Match".equals(level))   return "badge-success";
        if ("Moderate Match".equals(level)) return "badge-warning";
        if ("Weak Match".equals(level))     return "badge-danger";
        return "badge-neutral";
    }
}
