package com.abi.fx.tutorial.dictionaryapplication;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Optional;

public class HelloController {
    @FXML
    private ListView<Word> wordListView;
    @FXML
    private TextField searchField;
    @FXML
    private TextArea definitionArea;
    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;

    @FXML
    public void initialize() {
        loadWords();

        wordListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                definitionArea.setText(newVal.getDefinition());
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal == null || newVal.isEmpty()) {
                    wordListView.setItems(Word.getAllWords());
                } else {
                    wordListView.setItems(Word.searchWords(newVal));
                }
            } catch (SQLException e) {
                showError("Search Error", "Failed to search for words: " + e.getMessage());
            }
        });
    }

    private void loadWords() {
        try {
            wordListView.setItems(Word.getAllWords());
        } catch (SQLException e) {
            showError("Loading Error", "Failed to load dictionary words: " + e.getMessage());
        }
    }

    @FXML
    protected void onAddButtonClick() {
        Dialog<Word> dialog = new Dialog<>();
        dialog.setTitle("Add New Word");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField termField = new TextField();
        termField.setPromptText("Enter word");
        TextArea defArea = new TextArea();
        defArea.setPromptText("Enter definition");
        defArea.setPrefRowCount(3);

        dialogPane.setContent(new VBox(8, termField, defArea));

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new Word(termField.getText(), defArea.getText());
            }
            return null;
        });

        Optional<Word> result = dialog.showAndWait();
        result.ifPresent(word -> {
            try {
                word.save();
                loadWords(); // Refresh the list
            } catch (SQLException e) {
                showError("Save Error", "Failed to save the word: " + e.getMessage());
            }
        });
    }

    @FXML
    protected void onRemoveButtonClick() {
        Word selectedWord = wordListView.getSelectionModel().getSelectedItem();
        if (selectedWord != null) {
            try {
                selectedWord.delete();
                loadWords(); // Refresh the list
                definitionArea.clear();
            } catch (SQLException e) {
                showError("Delete Error", "Failed to delete the word: " + e.getMessage());
            }
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
