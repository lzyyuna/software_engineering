package com.group4.tarecruitment.view;

import com.group4.tarecruitment.model.Admin;
import com.group4.tarecruitment.service.AIWorkloadService;
import com.group4.tarecruitment.service.AdminService;
import com.group4.tarecruitment.service.AdminServiceImpl;
import com.group4.tarecruitment.util.ThemeManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class AdminWorkloadView {

    private final Stage stage;
    private final AdminService adminService = new AdminServiceImpl();
    private ObservableList<Admin> masterData = FXCollections.observableArrayList();
    private ObservableList<Admin> filteredData = FXCollections.observableArrayList();
    private TableView<Admin> workloadTable;

    private double currentThreshold = 10.0;
    private final Runnable onBack;

    public AdminWorkloadView(Stage stage, Runnable onBack) {
        this.stage = stage;
        this.onBack = onBack;
        loadThreshold();
    }

    private void loadThreshold() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("data/admin_settings.properties")) {
            props.load(fis);
            currentThreshold = Double.parseDouble(props.getProperty("threshold", "10.0"));
        } catch (Exception e) {
            currentThreshold = 10.0;
        }
    }

    private void saveThreshold() {
        Properties props = new Properties();
        props.setProperty("threshold", String.valueOf(currentThreshold));
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        try (FileOutputStream fos = new FileOutputStream("data/admin_settings.properties")) {
            props.store(fos, "Admin Settings");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Parent createContent() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab workloadTab = new Tab("TA Workload Overview");
        workloadTab.setContent(createWorkloadTab());

        Tab statsTab = new Tab("Statistics");
        statsTab.setContent(createStatsTab());

        tabPane.getTabs().addAll(workloadTab, statsTab);

        VBox root = new VBox(tabPane);
        root.getStyleClass().add("app-page");
        return root;
    }

    private Parent createWorkloadTab() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("app-page");

        Label mainTitle = new Label("TA Workload Overview");
        mainTitle.getStyleClass().add("page-title");

        HBox headerButtonsBox = new HBox(10);
        headerButtonsBox.setAlignment(Pos.CENTER_LEFT);
        headerButtonsBox.getStyleClass().add("toolbar");

        Button backBtn = new Button("Back to Dashboard");
        backBtn.getStyleClass().add("btn-success");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-warning");

        Button exportBtn = new Button("Export CSV");
        exportBtn.getStyleClass().add("btn-info");

        Button aiAnalysisBtn = new Button("AI Analysis");
        aiAnalysisBtn.getStyleClass().add("btn-danger");

        Label thresholdLabel = new Label("Weekly Threshold:");
        thresholdLabel.getStyleClass().add("login-label");
        TextField thresholdInput = new TextField(String.valueOf(currentThreshold));
        thresholdInput.setPrefWidth(70);
        Button setThresholdBtn = new Button("Set Threshold");
        setThresholdBtn.getStyleClass().add("btn-purple");

        headerButtonsBox.getChildren().addAll(refreshBtn, exportBtn, aiAnalysisBtn,
                setThresholdBtn, thresholdLabel, thresholdInput, backBtn);

        VBox whiteCard = new VBox(14);
        whiteCard.getStyleClass().add("surface-card");
        VBox.setVgrow(whiteCard, Priority.ALWAYS);

        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField taNameFilter = new TextField();
        taNameFilter.setPromptText("Filter by TA Name");
        TextField courseFilter = new TextField();
        courseFilter.setPromptText("Filter by Course");
        TextField moFilter = new TextField();
        moFilter.setPromptText("Filter by Hiring MO");

        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("btn-info");
        Button clearBtn = new Button("Clear Filters");
        clearBtn.getStyleClass().add("btn-muted");

        filterBar.getChildren().addAll(taNameFilter, courseFilter, moFilter, searchBtn, clearBtn);

        workloadTable = new TableView<>();
        setupTableColumns();
        workloadTable.setItems(filteredData);
        VBox.setVgrow(workloadTable, Priority.ALWAYS);

        whiteCard.getChildren().addAll(filterBar, workloadTable);
        root.getChildren().addAll(mainTitle, headerButtonsBox, whiteCard);

        loadData();

        setThresholdBtn.setOnAction(e -> {
            try {
                currentThreshold = Double.parseDouble(thresholdInput.getText().trim());
                saveThreshold();
                applyThreshold();
                workloadTable.refresh();
            } catch (Exception ex) {
                showAlert("Error", "Invalid threshold value.");
            }
        });

        refreshBtn.setOnAction(e -> {
            loadData();
            workloadTable.refresh();
            showAlert("Success", "Data refreshed from CSV files.");
        });

        backBtn.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        searchBtn.setOnAction(e -> {
            String ta = taNameFilter.getText().trim().toLowerCase();
            String crs = courseFilter.getText().trim().toLowerCase();
            String mo = moFilter.getText().trim().toLowerCase();

            filteredData.setAll(masterData.stream()
                .filter(a -> ta.isEmpty() || (a.getTaName() != null && a.getTaName().toLowerCase().contains(ta)))
                .filter(a -> crs.isEmpty() || (a.getCourseName() != null && a.getCourseName().toLowerCase().contains(crs)))
                .filter(a -> mo.isEmpty() || (a.getHireMo() != null && a.getHireMo().toLowerCase().contains(mo)))
                .collect(Collectors.toList()));

            filteredData.sort((a, b) -> Double.compare(b.getTotalWorkload(), a.getTotalWorkload()));
            workloadTable.refresh();
        });

        clearBtn.setOnAction(e -> {
            taNameFilter.clear();
            courseFilter.clear();
            moFilter.clear();
            filteredData.setAll(masterData);
            filteredData.sort((a, b) -> Double.compare(b.getTotalWorkload(), a.getTotalWorkload()));
            workloadTable.refresh();
        });

        exportBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("ta_workload_export.csv");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    adminService.exportTaWorkloadData(filteredData, file.getAbsolutePath());
                    showAlert("Success", "Export successful.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Export failed: " + ex.getMessage());
                }
            }
        });

        aiAnalysisBtn.setOnAction(e -> runAiAnalysis());

        return root;
    }

    private void setupTableColumns() {
        TableColumn<Admin, String> idCol = new TableColumn<>("TA ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("taId"));

        TableColumn<Admin, String> nameCol = new TableColumn<>("TA Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("taName"));

        TableColumn<Admin, String> moCol = new TableColumn<>("Hiring MO");
        moCol.setCellValueFactory(new PropertyValueFactory<>("hireMo"));

        TableColumn<Admin, String> posCol = new TableColumn<>("Position");
        posCol.setCellValueFactory(new PropertyValueFactory<>("positionName"));

        TableColumn<Admin, String> crsCol = new TableColumn<>("Course");
        crsCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));

        TableColumn<Admin, Double> weekCol = new TableColumn<>("Weekly");
        weekCol.setCellValueFactory(new PropertyValueFactory<>("weeklyWorkload"));

        TableColumn<Admin, Double> totCol = new TableColumn<>("Total Workload");
        totCol.setCellValueFactory(new PropertyValueFactory<>("totalWorkload"));

        TableColumn<Admin, String> excCol = new TableColumn<>("Excess Details");
        excCol.setCellValueFactory(cellData -> {
            Admin item = cellData.getValue();
            if (item.getExcessAmount() > 0) {
                return new SimpleStringProperty(
                        String.format("%.1f - %.1f = %.1f", item.getTotalWorkload(), currentThreshold, item.getExcessAmount())
                );
            } else {
                return new SimpleStringProperty("");
            }
        });

        TableColumn<Admin, String> sugCol = new TableColumn<>("Suggestion");
        sugCol.setCellValueFactory(new PropertyValueFactory<>("suggestion"));

        TableColumn<Admin, String> dateCol = new TableColumn<>("Hire Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("hireDate"));

        workloadTable.getColumns().addAll(idCol, nameCol, moCol, posCol, crsCol, weekCol, totCol, excCol, sugCol, dateCol);

        workloadTable.setRowFactory(tv -> new TableRow<Admin>() {
            @Override
            protected void updateItem(Admin item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getExcessAmount() > 0) {
                    setStyle("-fx-text-inner-color: #b53e35; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void applyThreshold() {
        for (Admin a : masterData) {
            double excess = a.getTotalWorkload() - currentThreshold;
            if (excess > 0) {
                a.setExcessAmount(excess);
                a.setSuggestion("Reduce Workload");
            } else {
                a.setExcessAmount(0);
                a.setSuggestion("");
            }
        }
        masterData.sort((a, b) -> Double.compare(b.getExcessAmount(), a.getExcessAmount()));
    }

    private void loadData() {
        try {
            List<Admin> data = adminService.getTaWorkload();
            masterData.setAll(data);
            applyThreshold();
            filteredData.setAll(masterData);
            filteredData.sort((a, b) -> Double.compare(b.getTotalWorkload(), a.getTotalWorkload()));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load workload data.");
        }
    }

    private Parent createStatsTab() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("app-page");

        Label title = new Label("Workload Statistics");
        title.getStyleClass().add("page-title");

        HBox headerButtonsBox = new HBox(10);
        headerButtonsBox.setAlignment(Pos.CENTER_LEFT);
        headerButtonsBox.getStyleClass().add("toolbar");

        Button exportStatsBtn = new Button("Export Statistics CSV");
        exportStatsBtn.getStyleClass().add("btn-info");

        headerButtonsBox.getChildren().addAll(exportStatsBtn);

        VBox whiteCard = new VBox(14);
        whiteCard.getStyleClass().add("surface-card");
        VBox.setVgrow(whiteCard, Priority.ALWAYS);

        HBox tablesBox = new HBox(20);
        tablesBox.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(tablesBox, Priority.ALWAYS);

        VBox courseBox = new VBox(10);
        Label courseLabel = new Label("By Course");
        courseLabel.getStyleClass().add("section-title");
        courseBox.getChildren().add(courseLabel);
        TableView<StatRow> courseTable = new TableView<>();
        courseTable.getColumns().addAll(
            createStatCol("Course", "name"),
            createStatCol("Total TAs", "totalTas"),
            createStatCol("Total Workload", "totalWorkload"),
            createStatCol("Avg Workload", "avgWorkload")
        );
        courseBox.getChildren().add(courseTable);
        HBox.setHgrow(courseBox, Priority.ALWAYS);

        VBox deptBox = new VBox(10);
        Label deptLabel = new Label("By Department");
        deptLabel.getStyleClass().add("section-title");
        deptBox.getChildren().add(deptLabel);
        TableView<StatRow> deptTable = new TableView<>();
        deptTable.getColumns().addAll(
            createStatCol("Department", "name"),
            createStatCol("Total TAs", "totalTas"),
            createStatCol("Total Workload", "totalWorkload"),
            createStatCol("Avg Workload", "avgWorkload")
        );
        deptBox.getChildren().add(deptTable);
        HBox.setHgrow(deptBox, Priority.ALWAYS);

        Map<String, StatRow> courseStats = new HashMap<>();
        Map<String, StatRow> deptStats = new HashMap<>();

        for (Admin a : masterData) {
            String cName = a.getCourseName();
            if (cName == null || cName.isEmpty()) cName = "Unknown";
            courseStats.putIfAbsent(cName, new StatRow(cName));
            courseStats.get(cName).add(a.getTaId(), a.getWeeklyWorkload());

            String dName = a.getDepartment();
            if (dName == null || dName.isEmpty()) dName = "Unknown";
            deptStats.putIfAbsent(dName, new StatRow(dName));
            deptStats.get(dName).add(a.getTaId(), a.getWeeklyWorkload());
        }

        courseTable.getItems().setAll(courseStats.values());
        deptTable.getItems().setAll(deptStats.values());

        tablesBox.getChildren().addAll(courseBox, deptBox);
        whiteCard.getChildren().add(tablesBox);

        exportStatsBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Stats");
            fileChooser.setInitialFileName("statistics.csv");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try (java.io.PrintWriter pw = new java.io.PrintWriter(file, "UTF-8")) {
                    pw.println("Category,Name,Total TAs,Total Workload,Avg Workload");
                    for (StatRow r : courseStats.values()) {
                        pw.println("Course," + r.getName() + "," + r.getTotalTas() + "," + r.getTotalWorkload() + "," + r.getAvgWorkload());
                    }
                    for (StatRow r : deptStats.values()) {
                        pw.println("Department," + r.getName() + "," + r.getTotalTas() + "," + r.getTotalWorkload() + "," + r.getAvgWorkload());
                    }
                    showAlert("Success", "Export successful.");
                } catch (Exception ex) {
                    showAlert("Error", "Export failed: " + ex.getMessage());
                }
            }
        });

        root.getChildren().addAll(title, headerButtonsBox, whiteCard);
        return root;
    }

    private TableColumn<StatRow, String> createStatCol(String title, String property) {
        TableColumn<StatRow, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private void runAiAnalysis() {
        String apiKey = AIWorkloadService.getApiKeyFromEnv();
        if (apiKey == null || apiKey.isBlank()) {
            showAlert("API Key Missing",
                "DeepSeek API Key not found.\n\n" +
                "Please add it to: config/api_keys.properties\n" +
                "with the line: deepseek.api.key=sk-...\n\n" +
                "Or set environment variable: DEEPSEEK_API_KEY");
            return;
        }

        if (masterData.isEmpty()) {
            showAlert("No Data", "No workload data available to analyze.");
            return;
        }

        List<Admin> dataSnapshot = new ArrayList<>(filteredData.isEmpty() ? masterData : filteredData);
        final String finalApiKey = apiKey;

        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.initOwner(stage);
        loadingStage.setTitle("AI Analysis");
        ProgressIndicator spinner = new ProgressIndicator();
        Label loadingLabel = new Label("Analyzing workload with AI...");
        VBox loadingBox = new VBox(15, spinner, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(30));
        loadingStage.setScene(ThemeManager.createScene(loadingBox, 320, 160));
        loadingStage.show();

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                AIWorkloadService aiService = new AIWorkloadService(finalApiKey);
                return aiService.analyzeWorkload(dataSnapshot, currentThreshold);
            }
        };

        task.setOnSucceeded(ev -> {
            loadingStage.close();
            showAiResultDialog(task.getValue());
        });

        task.setOnFailed(ev -> {
            loadingStage.close();
            Throwable ex = task.getException();
            showAlert("AI Analysis Failed", ex.getMessage() != null ? ex.getMessage() : ex.toString());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void showAiResultDialog(String analysisResult) {
        Stage resultStage = new Stage();
        resultStage.initModality(Modality.APPLICATION_MODAL);
        resultStage.initOwner(stage);
        resultStage.setTitle("AI Workload Analysis");

        Label headerTitle = new Label("AI Workload Analysis");
        headerTitle.getStyleClass().add("page-title");

        Map<String, String> sections = parseSections(analysisResult);

        // Verdict badge
        String verdictRaw = sections.getOrDefault("VERDICT", "").toUpperCase();
        String verdictText;
        String badgeClass;
        if (verdictRaw.contains("CRITICAL")) {
            verdictText = "CRITICAL";  badgeClass = "badge-danger";
        } else if (verdictRaw.contains("NEEDS ATTENTION")) {
            verdictText = "NEEDS ATTENTION"; badgeClass = "badge-warning";
        } else if (verdictRaw.contains("ACCEPTABLE")) {
            verdictText = "ACCEPTABLE"; badgeClass = "badge-success";
        } else {
            verdictText = verdictRaw.isEmpty() ? "" : verdictRaw;
            badgeClass = "badge-neutral";
        }

        HBox bannerRow = new HBox();
        if (!verdictText.isEmpty()) {
            Label verdictLabel = new Label("Verdict: " + verdictText);
            verdictLabel.getStyleClass().addAll("badge", badgeClass);
            bannerRow.getChildren().add(verdictLabel);
            bannerRow.setAlignment(Pos.CENTER_LEFT);
        }

        String[][] sectionDefs = {
            {"ASSESSMENT", "Overall Assessment"},
            {"AT-RISK",    "At-Risk TAs"},
            {"ACTIONS",    "Recommended Actions"},
        };

        VBox cardsBox = new VBox(12);

        boolean anySectionFound = false;
        for (String[] def : sectionDefs) {
            String content = sections.get(def[0]);
            if (content == null || content.isBlank()) continue;
            anySectionFound = true;
            cardsBox.getChildren().add(buildSectionCard(def[1], content));
        }

        if (!anySectionFound) {
            cardsBox.getChildren().add(buildSectionCard("Analysis", analysisResult));
        }

        VBox content = new VBox(14, headerTitle, bannerRow, cardsBox);
        content.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button copyBtn = new Button("Copy to Clipboard");
        copyBtn.getStyleClass().add("btn-info");
        copyBtn.setOnAction(e -> {
            Clipboard cb = Clipboard.getSystemClipboard();
            ClipboardContent cc = new ClipboardContent();
            cc.putString(analysisResult);
            cb.setContent(cc);
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

        Scene scene = ThemeManager.createScene(root, 680, 560);
        resultStage.setScene(scene);
        resultStage.setMinWidth(480);
        resultStage.setMinHeight(360);
        resultStage.show();
    }

    private VBox buildSectionCard(String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);

        VBox card = new VBox(8, titleLabel, new Separator(), bodyLabel);
        card.getStyleClass().add("surface-card");
        return card;
    }

    private Map<String, String> parseSections(String text) {
        Map<String, String> result = new LinkedHashMap<>();
        String[] keys = {"ASSESSMENT", "AT-RISK", "ACTIONS", "VERDICT"};
        for (String key : keys) {
            int start = text.indexOf(key + ":");
            if (start == -1) continue;
            start += key.length() + 1;
            int end = text.length();
            for (String other : keys) {
                if (other.equals(key)) continue;
                int idx = text.indexOf(other + ":", start);
                if (idx != -1 && idx < end) end = idx;
            }
            result.put(key, text.substring(start, end).trim());
        }
        return result;
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public static class StatRow {
        private String name;
        private Set<String> taIds = new HashSet<>();
        private double totalWorkload;

        public StatRow(String name) {
            this.name = name;
            this.totalWorkload = 0.0;
        }

        public void add(String taId, double workload) {
            this.taIds.add(taId);
            this.totalWorkload += workload;
        }

        public String getName() { return name; }
        public int getTotalTas() { return taIds.size(); }
        public double getTotalWorkload() { return totalWorkload; }
        public String getAvgWorkload() {
            if (taIds.isEmpty()) return "0.00";
            return String.format("%.2f", totalWorkload / taIds.size());
        }
    }
}
