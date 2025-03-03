package org.viti.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerApp extends Application {
    private GridPane gridPane = new GridPane();
    private VBox userContainer = new VBox(10);
    private TextArea logArea = new TextArea();
    private Set<PrintWriter> clientWriters = new HashSet<>();
    private List<Person> people = new ArrayList<>(Arrays.asList(
            new Person("Полковник Гулій", false),
            new Person("Капітан Сидоренко", false),
            new Person("Майор Петренко", false),
            new Person("Лейтенант Іванов", false),
            new Person("Сержант Ковальчук", false),
            new Person("Солдат Лисенко", false),
            new Person("Солдат Ткаченко", false),
            new Person("Підполковник Мороз", false),
            new Person("Генерал-майор Бойко", false),
            new Person("Сержант Веремій", false),
            new Person("Полковник Левченко", false),
            new Person("Капітан Бондаренко", false),
            new Person("Майор Шевченко", false),
            new Person("Лейтенант Павленко", false),
            new Person("Сержант Ярмоленко", false),
            new Person("Солдат Козак", false),
            new Person("Солдат Соловйов", false),
            new Person("Підполковник Орлов", false),
            new Person("Генерал-майор Остром", false),
            new Person("Сержант Чорний", false),
            new Person("Полковник Малий", false),
            new Person("Капітан Тарасова", false),
            new Person("Майор Дмитрів", false),
            new Person("Лейтенант Курочкін", false),
            new Person("Сержант Гуменюк", false),
            new Person("Солдат Щербань", false),
            new Person("Солдат Мельник", false),
            new Person("Підполковник Зубов", false),
            new Person("Генерал-майор Дорошенко", false),
            new Person("Сержант Степаненко", false),
            new Person("Полковник Шульга", false),
            new Person("Капітан Новиков", false),
            new Person("Майор Кисельов", false)
    ));


    @Override
    public void start(Stage primaryStage) {
        logArea.setEditable(false);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        Text header = new Text("ТАБЛО КОНТРОЛЮ ПРИБУТТЯ");
        header.setFont(new Font("San Francisco", 28));
        header.setFill(Color.WHITE);
        header.setStyle("-fx-font-weight: bold;");
        header.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox headerBox = new VBox(10, header);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER);
        VBox.setMargin(header, new Insets(20, 0, 20, 0));

        ScrollPane scrollPane = new ScrollPane(userContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true); // Дозволяє скролити мишею
        scrollPane.setStyle("-fx-background: #445c36;");

        userContainer.setStyle("-fx-background-color: #445c36; -fx-padding: 20px;");
        updateUserView(gridPane);

        MenuBar menuBar = new MenuBar();
        Menu actionsMenu = new Menu("МЕНЮ");
        MenuItem addUserItem = new MenuItem("Додати користувача");
        MenuItem editUserItem = new MenuItem("Редагувати користувача");
        MenuItem removeUserItem = new MenuItem("Видалити користувача");
        MenuItem exportWordItem = new MenuItem("Експортувати в Word");
        addUserItem.setOnAction(e -> showAddDialog());
        editUserItem.setOnAction(e -> showEditDialog());
        removeUserItem.setOnAction(e -> showRemoveDialog());
        exportWordItem.setOnAction(e -> exportToWord(primaryStage));
        actionsMenu.getItems().addAll(addUserItem, editUserItem, removeUserItem, exportWordItem);
        menuBar.getMenus().add(actionsMenu);
        menuBar.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000; ");

        VBox root = new VBox();
        root.setStyle("-fx-padding: 20; -fx-background-color: #445c36");
        root.getChildren().addAll(menuBar, headerBox, scrollPane);

        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setTitle("ТАБЛО КОНТРОЛЮ ПРИБУТТЯ");
        primaryStage.show();

        new Thread(this::startServer).start();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(5003)) {
            log("Сервер запущено на порту 5003...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("Новий клієнт підключився: " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            log("Помилка сервера: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            synchronized (clientWriters) {
                clientWriters.add(writer);
            }

            sendUserList(writer);

            String message;
            while ((message = reader.readLine()) != null) {
                log("Отримано: " + message);
                handleClientMessage(message);
                broadcastMessage(message);
            }
        } catch (IOException e) {
            log("Клієнт відключився.");
        }
    }

    private void sendUserList(PrintWriter writer) {
        for (Person person : people) {
            writer.println("USER:" + person.getName() + ":" + (person.isArrived() ? "Прибув" : "Вибув"));
        }
    }

    private void handleClientMessage(String message) {
        log("Обробка повідомлення: " + message);

        if (message.startsWith("Прибув:") || message.startsWith("Вибув:")) {
            String[] parts = message.split(":");
            String status = parts[0];
            String name = parts[1];

            for (Person person : people) {
                if (person.getName().equals(name)) {
                    log("Знайдено користувача: " + name);
                    person.setArrived(status.equals("Прибув"));
                    log("Статус змінено на '" + status + "' для " + name);
                    updateUserView(gridPane);
                    broadcastMessage(status + ":" + name);
                    break;
                }
            }
        }
    }

    private void broadcastMessage(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }


    private List<String> getUserListAsStrings() {
        List<String> result = new ArrayList<>();
        for (Person person : people) {
            result.add(person.getName() + " - " + (person.isArrived() ? "Прибув" : "Вибув"));
        }
        return result;
    }

    private void log(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    private void showAddDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Додати користувача");
        dialog.setHeaderText("Введіть ім'я нового користувача:");
        dialog.setContentText("Ім'я:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            people.add(new Person(name, false));
            log("Додано нового користувача: " + name);
            updateUserView(gridPane);
            broadcastMessage("USER:" + name + ":Вибув");
        });
    }

    private void showEditDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(people.get(0).getName(), getUserNames());
        dialog.setTitle("Редагувати користувача");
        dialog.setHeaderText("Виберіть користувача для редагування:");
        dialog.setContentText("Ім'я користувача:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            TextInputDialog editDialog = new TextInputDialog(name);
            editDialog.setTitle("Редагувати ім'я");
            editDialog.setHeaderText("Введіть нове ім'я для користувача:");
            editDialog.setContentText("Нове ім'я:");

            Optional<String> newName = editDialog.showAndWait();
            newName.ifPresent(updatedName -> {
                for (Person person : people) {
                    if (person.getName().equals(name)) {
                        person.setName(updatedName);
                        log("Ім'я користувача змінено з '" + name + "' на '" + updatedName + "'");
                        updateUserView(gridPane);
                        broadcastMessage("EDIT:" + name + ":" + updatedName);
                        break;
                    }
                }
            });
        });
    }

    private void showRemoveDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(people.get(0).getName(), getUserNames());
        dialog.setTitle("Видалити користувача");
        dialog.setHeaderText("Виберіть користувача для видалення:");
        dialog.setContentText("Ім'я користувача:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            people.removeIf(person -> person.getName().equals(name));
            log("Користувача '" + name + "' видалено.");
            updateUserView(gridPane);
        });
    }

    private void updateUserView(GridPane gridPane) {
        Platform.runLater(() -> {
            userContainer.getChildren().clear();

            double personBlockSize = 150;
            double fontSize = 14;
            int maxPerRow = 10;

            HBox row = createNewRow();

            for (Person person : people) {
                VBox personBlock = new VBox(5);
                personBlock.setPrefSize(personBlockSize, personBlockSize);
                personBlock.setStyle("-fx-border-radius: 16px; -fx-background-radius: 16px; -fx-padding: 10px; -fx-border-color: white; -fx-border-width: 1px; -fx-background-color: white;");
                personBlock.setAlignment(Pos.CENTER);

                String[] nameParts = person.getName().split(" ", 2);
                String rank = nameParts.length > 1 ? nameParts[0] : "";
                String surname = nameParts.length > 1 ? nameParts[1] : nameParts[0];

                Label rankLabel = new Label(rank);
                rankLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000; -fx-font-size: " + fontSize + "px; -fx-alignment: center;");
                rankLabel.setMaxWidth(personBlockSize - 20);
                rankLabel.setWrapText(true);
                rankLabel.setAlignment(Pos.CENTER);

                Label surnameLabel = new Label(surname);
                surnameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000; -fx-font-size: " + fontSize + "px; -fx-alignment: center;");
                surnameLabel.setMaxWidth(personBlockSize - 20);
                surnameLabel.setWrapText(true);
                surnameLabel.setAlignment(Pos.CENTER);

                Label statusLabel = new Label(person.isArrived() ? "Прибув" : "Вибув");
                statusLabel.setStyle(person.isArrived() ? "-fx-text-fill: green; -fx-alignment: center;" : "-fx-text-fill: red; -fx-alignment: center;");
                statusLabel.setMaxWidth(personBlockSize - 20);
                statusLabel.setWrapText(true);
                statusLabel.setAlignment(Pos.CENTER);

                personBlock.getChildren().addAll(rankLabel, surnameLabel, statusLabel);
                row.getChildren().add(personBlock);

                if (row.getChildren().size() == maxPerRow) {
                    userContainer.getChildren().add(row);
                    row = createNewRow();
                }
            }
        });
    }

    private HBox createNewRow() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);
        return row;
    }






    private List<String> getUserNames() {
        List<String> names = new ArrayList<>();
        for (Person person : people) {
            names.add(person.getName());
        }
        return names;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void exportToWord(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Документ Word", "*.docx"));

        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try {
                XWPFDocument document = new XWPFDocument();

                XWPFParagraph title = document.createParagraph();
                title.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun run = title.createRun();
                run.setBold(true);
                run.setFontSize(18);
                run.setText("Список людей та їх статуси");

                XWPFTable table = document.createTable();
                XWPFTableRow headerRow = table.getRow(0);
                headerRow.getCell(0).setText("Ім'я");
                headerRow.addNewTableCell().setText("Статус");

                for (Person person : people) {
                    XWPFTableRow row = table.createRow();
                    row.getCell(0).setText(person.getName());
                    row.getCell(1).setText(person.isArrived() ? "Прибув" : "Вибув");
                }

                try (FileOutputStream out = new FileOutputStream(file)) {
                    document.write(out);
                }

                log("Файл Word збережено: " + file.getAbsolutePath());
            } catch (IOException e) {
                log("Помилка при експортуванні у Word: " + e.getMessage());
            }
        } else {
            log("Збереження скасовано.");
        }
    }
}