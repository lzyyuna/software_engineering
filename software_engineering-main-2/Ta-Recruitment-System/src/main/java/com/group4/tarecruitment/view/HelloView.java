package com.group4.tarecruitment.view;

import com.group4.tarecruitment.controller.HelloController;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class HelloView {

    public Parent createContent() {
        HelloController controller = new HelloController();

        // --- UI组件 ---
        Label titleLabel = new Label("TA Recruitment System - Applicant Registration");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your full name");

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your university email");

        Label skillsLabel = new Label("Skills:");
        TextField skillsField = new TextField();
        skillsField.setPromptText("e.g., Java, Python, English, Teaching");

        Button submitButton = new Button("Register as TA Applicant");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Label resultLabel = new Label("");
        resultLabel.setStyle("-fx-text-fill: #FF0000;"); // 结果文字用红色显示

        // --- 绑定按钮事件 ---
        // 当按钮被点击时，调用控制器的handleRegistration方法，并传入所有输入框和结果标签
        submitButton.setOnAction(e -> controller.handleRegistration(nameField, emailField, skillsField, resultLabel));

        // --- 布局 ---
        VBox root = new VBox(10); // 垂直布局，组件间距10px
        root.setPadding(new Insets(20)); // 内边距20px
        root.getChildren().addAll(
                titleLabel,
                nameLabel, nameField,
                emailLabel, emailField,
                skillsLabel, skillsField,
                submitButton,
                resultLabel
        );

        return root;
    }
}