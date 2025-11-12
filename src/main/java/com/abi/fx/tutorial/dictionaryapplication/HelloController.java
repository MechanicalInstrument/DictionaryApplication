package com.abi.fx.tutorial.dictionaryapplication;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;
import java.util.Optional;

public class HelloController {
    @FXML
    private ListView<Object> wordListView;
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
        // Set up custom cell factory for rendering sections and words
        wordListView.setCellFactory(param -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (item instanceof SectionHeader) {
                    // Style section headers
                    SectionHeader header = (SectionHeader) item;
                    Label headerLabel = new Label(header.letter());
                    headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    headerLabel.setTextFill(Color.web("#1f3a93"));
                    headerLabel.setPadding(new Insets(10, 5, 5, 5));

                    setGraphic(headerLabel);
                    setText(null);
                    setStyle("-fx-background-color: #f0f0f0;");
                } else if (item instanceof Word) {
                    // Style regular word items
                    Word word = (Word) item;
                    setText(word.getTerm());
                    setGraphic(null);
                    setStyle("");
                }
            }
        });

        loadWords();

        wordListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal instanceof Word) {
                Word word = (Word) newVal;
                // Display all definitions with numbering if there are multiple
                if (word.getDefinitions().size() > 1) {
                    StringBuilder allDefinitions = new StringBuilder();
                    int count = 1;
                    for (String def : word.getDefinitions()) {
                        allDefinitions.append(count).append(". ").append(def).append("\n\n");
                        count++;
                    }
                    definitionArea.setText(allDefinitions.toString().trim());
                } else {
                    definitionArea.setText(word.getDefinition());
                }
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal == null || newVal.isEmpty()) {
                    loadWords();
                } else {
                    loadSearchResults(Word.searchWords(newVal));
                }
            } catch (SQLException e) {
                showError("Search Error", "Failed to search for words: " + e.getMessage());
            }
        });
    }

    private void loadWords() {
        try {
            ObservableList<Word> allWords = Word.getAllWords();
            ObservableList<Object> organizedItems = organizeWordsWithSections(allWords);
            wordListView.setItems(organizedItems);
        } catch (SQLException e) {
            showError("Loading Error", "Failed to load dictionary words: " + e.getMessage());
        }
    }

    private void loadSearchResults(ObservableList<Word> searchResults) {
        ObservableList<Object> organizedItems = organizeWordsWithSections(searchResults);
        wordListView.setItems(organizedItems);
    }

    /**
     * Organizes words with alphabetical section headers
     * Example: [SectionHeader("A"), Word("apple"), Word("apricot"), SectionHeader("B"), Word("banana")]
     */
    private ObservableList<Object> organizeWordsWithSections(ObservableList<Word> words) {
        ObservableList<Object> organizedList = FXCollections.observableArrayList();
        String currentSection = "";

        for (Word word : words) {
            String firstLetter = word.getTerm().substring(0, 1).toUpperCase();

            // Add section header if letter changes
            if (!firstLetter.equals(currentSection)) {
                organizedList.add(new SectionHeader(firstLetter));
                currentSection = firstLetter;
            }

            // Add the word
            organizedList.add(word);
        }

        return organizedList;
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
        Object selectedItem = wordListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem instanceof Word) {
            Word selectedWord = (Word) selectedItem;
            try {
                selectedWord.delete();
                loadWords(); // Refresh the list
                definitionArea.clear();
            } catch (SQLException e) {
                showError("Delete Error", "Failed to delete the word: " + e.getMessage());
            }
        } else {
            showError("Delete Error", "Please select a word to delete");
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
