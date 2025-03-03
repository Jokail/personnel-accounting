package org.viti.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ModeSelectionApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Вибір режиму");


        Label headerLabel = new Label("ТАБЛО ВІДВІДУВАННЯ");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Button serverButton = new Button("Сервер");
        serverButton.setStyle("-fx-font-size: 18px; -fx-background-color: #ededed; -fx-text-fill: #000000; -fx-border-radius: 8px; -fx-padding: 10px;");
        serverButton.setOnAction(e -> openServerWindow());

        Button clientButton = new Button("Клієнт");
        clientButton.setStyle("-fx-font-size: 18px; -fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-border-radius: 8px; -fx-padding: 10px;");
        clientButton.setOnAction(e -> openClientWindow());

        VBox buttonBox = new VBox(20);
        buttonBox.getChildren().addAll(serverButton, clientButton);
        buttonBox.setStyle("-fx-padding: 30px; -fx-alignment: center; -fx-background-color: #445c36;");

        VBox root = new VBox(20);
        root.getChildren().addAll(headerLabel, buttonBox);
        root.setStyle("-fx-padding: 30px; -fx-alignment: center; -fx-background-color: #445c36;");

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openServerWindow() {
        ServerApp serverApp = new ServerApp();
        serverApp.start(new Stage());
    }

    private void openClientWindow() {
        ClientApp clientApp = new ClientApp();
        clientApp.start(new Stage());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
