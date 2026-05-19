package com.group4.tarecruitment.view;

import com.group4.tarecruitment.model.InviteCode;
import com.group4.tarecruitment.model.User;
import com.group4.tarecruitment.service.AdminService;
import com.group4.tarecruitment.service.AdminServiceImpl;
import com.group4.tarecruitment.service.InviteCodeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class AdminUserView {

    private final AdminService adminService = new AdminServiceImpl();
    private final InviteCodeService inviteCodeService = new InviteCodeService();
    private final Runnable onBack;
    private final ObservableList<User> userData = FXCollections.observableArrayList();
    private final ObservableList<InviteCode> inviteData = FXCollections.observableArrayList();

    public AdminUserView(Stage stage, Runnable onBack) {
        this.onBack = onBack;
    }

    public Parent createContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(28));
        root.getStyleClass().add("app-page");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("User Management");
        title.getStyleClass().add("page-title");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("Back to Dashboard");
        backBtn.getStyleClass().add("btn-success");
        backBtn.setOnAction(e -> onBack.run());

        header.getChildren().addAll(title, spacer, backBtn);

        // ── User List Card ──────────────────────────────────────────────────
        VBox userCard = new VBox(14);
        userCard.getStyleClass().add("surface-card");
        VBox.setVgrow(userCard, Priority.ALWAYS);

        Label userCardTitle = new Label("Registered Users");
        userCardTitle.getStyleClass().add("section-title");

        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by username...");
        searchField.setPrefWidth(250);

        ComboBox<String> roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("All Roles", "Admin", "TA", "MO");
        roleFilter.getSelectionModel().selectFirst();
        roleFilter.setPrefWidth(150);

        Label filterLabel = new Label("Filter:");
        controls.getChildren().addAll(filterLabel, searchField, roleFilter);

        TableView<User> userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userCol.setMaxWidth(1f * Integer.MAX_VALUE * 60);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setMaxWidth(1f * Integer.MAX_VALUE * 40);

        userTable.getColumns().addAll(userCol, roleCol);
        VBox.setVgrow(userTable, Priority.ALWAYS);

        javafx.collections.transformation.FilteredList<User> filteredData =
                new javafx.collections.transformation.FilteredList<>(userData, p -> true);

        searchField.textProperty().addListener((obs, o, n) ->
                updatePredicate(filteredData, n, roleFilter.getValue()));
        roleFilter.valueProperty().addListener((obs, o, n) ->
                updatePredicate(filteredData, searchField.getText(), n));

        javafx.collections.transformation.SortedList<User> sortedData =
                new javafx.collections.transformation.SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);

        userCard.getChildren().addAll(userCardTitle, controls, userTable);

        // ── Invite Code Card ────────────────────────────────────────────────
        VBox inviteCard = new VBox(14);
        inviteCard.getStyleClass().add("surface-card");
        VBox.setVgrow(inviteCard, Priority.ALWAYS);

        Label inviteCardTitle = new Label("Invite Code Management");
        inviteCardTitle.getStyleClass().add("section-title");

        HBox generateRow = new HBox(12);
        generateRow.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> roleSelector = new ComboBox<>();
        roleSelector.getItems().addAll("TA", "MO");
        roleSelector.setValue("TA");
        roleSelector.setPrefWidth(100);

        Button generateBtn = new Button("Generate Invite Code");
        generateBtn.getStyleClass().add("btn-info");

        Label generatedCodeLabel = new Label();
        generatedCodeLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");

        Button copyBtn = new Button("Copy");
        copyBtn.getStyleClass().add("btn-muted");
        copyBtn.setVisible(false);

        generateBtn.setOnAction(e -> {
            String role = roleSelector.getValue();
            String code = inviteCodeService.generateCode(role);
            generatedCodeLabel.setText("New code: " + code);
            copyBtn.setVisible(true);
            copyBtn.setOnAction(ev -> {
                ClipboardContent content = new ClipboardContent();
                content.putString(code);
                Clipboard.getSystemClipboard().setContent(content);
            });
            refreshInviteCodes();
        });

        Label roleLabel = new Label("Role:");
        generateRow.getChildren().addAll(roleLabel, roleSelector, generateBtn, generatedCodeLabel, copyBtn);

        TableView<InviteCode> inviteTable = new TableView<>();
        inviteTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(inviteTable, Priority.ALWAYS);

        TableColumn<InviteCode, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setMaxWidth(1f * Integer.MAX_VALUE * 35);

        TableColumn<InviteCode, String> inviteRoleCol = new TableColumn<>("Role");
        inviteRoleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        inviteRoleCol.setMaxWidth(1f * Integer.MAX_VALUE * 15);

        TableColumn<InviteCode, Boolean> usedCol = new TableColumn<>("Used");
        usedCol.setCellValueFactory(new PropertyValueFactory<>("used"));
        usedCol.setMaxWidth(1f * Integer.MAX_VALUE * 15);

        TableColumn<InviteCode, String> dateCol = new TableColumn<>("Created");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setMaxWidth(1f * Integer.MAX_VALUE * 35);

        inviteTable.getColumns().addAll(codeCol, inviteRoleCol, usedCol, dateCol);
        inviteTable.setItems(inviteData);

        inviteCard.getChildren().addAll(inviteCardTitle, generateRow, inviteTable);

        root.getChildren().addAll(header, userCard, inviteCard);

        loadUsers();
        refreshInviteCodes();

        return root;
    }

    private void updatePredicate(javafx.collections.transformation.FilteredList<User> filteredList,
                                  String username, String role) {
        filteredList.setPredicate(user -> {
            boolean matchesName = (username == null || username.isEmpty()) ||
                                  user.getUsername().toLowerCase().contains(username.toLowerCase());
            boolean matchesRole = (role == null || role.equals("All Roles")) ||
                                  user.getRole().equalsIgnoreCase(role);
            return matchesName && matchesRole;
        });
    }

    private void loadUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            userData.setAll(users);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load user data from user.csv.");
        }
    }

    private void refreshInviteCodes() {
        inviteData.setAll(inviteCodeService.getAllCodes());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
