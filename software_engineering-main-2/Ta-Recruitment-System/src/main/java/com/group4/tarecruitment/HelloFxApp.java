package com.group4.tarecruitment;

import com.group4.tarecruitment.view.HelloView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloFxApp extends Application {

    @Override
    public void start(Stage stage) {
        HelloView view = new HelloView();
        Scene scene = new Scene(view.createContent(), 600, 450);

        stage.setTitle("TA Recruitment System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}