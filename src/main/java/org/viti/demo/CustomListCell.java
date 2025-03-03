package org.viti.demo;

import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.Map;

public class CustomListCell extends ListCell<String> {
    private final Map<String, Button> buttonMap;
    private final HBox container = new HBox(10);
    private final Text nameText = new Text();

    public CustomListCell(Map<String, Button> buttonMap) {
        this.buttonMap = buttonMap;
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            nameText.setText(item);
            Button button = buttonMap.getOrDefault(item.split(" - ")[0], new Button("Прибув"));
            container.getChildren().setAll(nameText, button);
            setGraphic(container);
        }
    }
}
