package org.viti.demo;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientApp extends Application {
    private GridPane gridPane = new GridPane();
    private PrintWriter writer;
    private Map<String, Button> buttonMap = new HashMap<>();
    private final int maxColumns = 7;
    private final int minColumns = 3;
    private String serverIP;

    @Override
    public void start(Stage primaryStage) {
        Label ipLabel = new Label("Введіть IP адресу:");
        ipLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        VBox inputBox = new VBox(10);
        inputBox.setAlignment(javafx.geometry.Pos.CENTER);
        inputBox.setStyle("-fx-background-color: #445c36; -fx-padding: 50;");

        TextField ipField = new TextField();
        ipField.setPromptText("Enter server IP address");
        ipField.setStyle("-fx-background-color: #fbfbfb; -fx-text-fill: #000000; -fx-font-size: 16px;");
        ipField.setPrefWidth(300);

        Button connectButton = new Button("Connect");
        connectButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-font-size: 16px;");
        connectButton.setOnAction(e -> {
            serverIP = ipField.getText();
            if (serverIP != null && !serverIP.isEmpty()) {
                inputBox.setVisible(false);  // Hide the input screen
                createMainUI(primaryStage);
                new Thread(this::connectToServer).start();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid IP address", ButtonType.OK);
                alert.showAndWait();
            }
        });

        inputBox.getChildren().addAll(ipLabel, ipField, connectButton);

        Scene inputScene = new Scene(inputBox, 600, 400);
        primaryStage.setScene(inputScene);
        primaryStage.setTitle("Enter Server IP");
        primaryStage.show();
    }

    private void createMainUI(Stage primaryStage) {
        gridPane.setHgap(15);
        gridPane.setVgap(15);

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(20));
        scrollPane.setStyle("-fx-background: #445c36;");

        VBox root = new VBox();
        root.setStyle("-fx-background-color: #445c36;");
        root.setAlignment(javafx.geometry.Pos.CENTER);

        Image backgroundImage = new Image("file:src/main/resources/photo/A0334.png");

        BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false);
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                backgroundSize
        );


        root.setBackground(new Background(background));
        root.setStyle("-fx-background-color: #445c36;");


        VBox contentVBox = new VBox(10);
        contentVBox.setAlignment(javafx.geometry.Pos.CENTER);
        contentVBox.setStyle("-fx-background-color: #445c36; -fx-padding: 20px;");

        Text header = new Text("ТАБЛО КОНТРОЛЮ ПРИБУТТЯ");
        header.setFont(new Font("San Francisco", 28));
        header.setFill(Color.WHITE);
        header.setStyle("-fx-font-weight: bold;");
        header.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox headerBox = new VBox(10, header);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER);
        VBox.setMargin(header, new Insets(20, 0, 20, 0));

        root.getChildren().addAll(headerBox, scrollPane);

        Scene scene = new Scene(root, 1200, 800);
        scene.widthProperty().addListener((observable, oldValue, newValue) -> updateColumns(newValue.doubleValue()));
        primaryStage.setScene(scene);
        primaryStage.setTitle("Клієнт (Apple Style)");
        primaryStage.show();
    }

    private void updateColumns(double width) {
        int columns = Math.max(minColumns, Math.min(maxColumns, (int) (width / 170)));
        gridPane.getColumnConstraints().clear();

        for (int i = 0; i < columns; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / columns);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
    }

    private void connectToServer() {
        try (Socket socket = new Socket(serverIP, 5003);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            this.writer = writer;

            String message;
            while ((message = reader.readLine()) != null) {
                processServerMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Помилка підключення до сервера.");
        }
    }

    private void processServerMessage(String message) {
        if (message.startsWith("USER:")) {
            String[] parts = message.split(":");
            String name = parts[1];
            String status = parts[2];

            Platform.runLater(() -> addUserBlock(name, status));
        } else if (message.startsWith("ARRIVED:") || message.startsWith("DROPPED_OUT:")) {
            String[] parts = message.split(":");
            String name = parts[1];
            String status = message.startsWith("ARRIVED") ? "Прибув" : "Вибув";

            Platform.runLater(() -> updateUserStatus(name, status));
        }
    }

    private void updateUserStatus(String name, String status) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (node instanceof VBox box) {
                Text statusText = (Text) box.getChildren().get(1);
                Button button = (Button) box.getChildren().get(2);

                if (statusText.getText().contains(name)) {
                    statusText.setText(status);
                    statusText.setFill(status.equals("Прибув") ? Color.LIMEGREEN : Color.RED);
                    button.setText(status.equals("Прибув") ? "Вибув" : "Прибув");
                    break;
                }
            }
        }
    }

    private void addUserBlock(String name, String status) {
        VBox block = createUserBlock(name, status);
        int count = gridPane.getChildren().size();
        int col = count % maxColumns;
        int row = count / maxColumns;
        gridPane.add(block, col, row);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(500), block);
        scaleTransition.setFromX(0);
        scaleTransition.setToX(1);
        scaleTransition.setFromY(0);
        scaleTransition.setToY(1);
        scaleTransition.play();
    }

    private VBox createUserBlock(String name, String status) {
        VBox box = new VBox(10);
        box.setPrefSize(160, 180);
        box.setStyle("-fx-background-color: #F8F8F8; -fx-border-radius: 16px; -fx-background-radius: 16px; -fx-padding: 12px; -fx-border-color: #000000;");
        box.setEffect(new javafx.scene.effect.DropShadow(10, Color.GRAY));

        box.setOnMouseEntered(e -> {
            box.setStyle("-fx-background-color: #EAEAEA; -fx-border-radius: 16px; -fx-background-radius: 16px; -fx-padding: 12px; -fx-border-color: #000000;");
            fadeInBox(box);
        });
        box.setOnMouseExited(e -> {
            box.setStyle("-fx-background-color: #F8F8F8; -fx-border-radius: 16px; -fx-background-radius: 16px; -fx-padding: 12px; -fx-border-color: #000000;");
        });

        String[] nameParts = name.split(" ");
        Text rankText = new Text(nameParts[0]);
        rankText.setFont(new Font("San Francisco", 18));

        Text surnameText = new Text(nameParts.length > 1 ? nameParts[1] : "");
        surnameText.setFont(new Font("San Francisco", 20));

        Text statusText = new Text(status);
        statusText.setFont(new Font("San Francisco", 16));
        statusText.setFill(status.equals("Прибув") ? Color.LIMEGREEN : Color.RED);
        statusText.setStyle("-fx-text-alignment: center;");

        Button button = new Button(status.equals("Прибув") ? "Вибув" : "Прибув");
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px;");
        button.setMinWidth(120);
        button.setMinHeight(40);
        button.setStyle("-fx-background-radius: 12px;");
        button.setOnAction(e -> toggleArrivalStatus(name, button, statusText));

        VBox textBox = new VBox(5, rankText, surnameText, statusText);
        textBox.setAlignment(javafx.geometry.Pos.CENTER);

        HBox buttonBox = new HBox(button);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        box.getChildren().addAll(textBox, buttonBox);
        buttonMap.put(name, button);

        return box;
    }

    private void fadeInBox(VBox box) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), box);
        fadeTransition.setFromValue(0.7);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private void toggleArrivalStatus(String name, Button button, Text statusText) {
        String newStatus = statusText.getText().equals("Прибув") ? "Вибув" : "Прибув";
        button.setText(newStatus.equals("Прибув") ? "Вибув" : "Прибув");
        statusText.setText(newStatus);
        statusText.setFill(newStatus.equals("Прибув") ? Color.LIMEGREEN : Color.RED);
        sendStatusToServer(name, newStatus);
    }

    private void sendStatusToServer(String name, String status) {
        if (writer != null) {
            writer.println(status + ":" + name);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
