package com.abi.fx.tutorial.dictionaryapplication;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.*;

public class Word {
    private Long id;
    private String term;
    private String definition;
    private ObservableList<String> definitions;

    public Word(String term, String definition) {
        this.term = term;
        this.definition = definition;
        this.definitions = FXCollections.observableArrayList(definition);
    }

    public Word(Long id, String term, String definition) {
        this.id = id;
        this.term = term;
        this.definition = definition;
        this.definitions = FXCollections.observableArrayList(definition);
    }

    public Word(String term, ObservableList<String> definitions) {
        this.term = term;
        this.definitions = definitions;
        this.definition = String.join("\n\n", definitions);
    }

    public Long getId() {
        return id;
    }

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }

    public ObservableList<String> getDefinitions() {
        return definitions;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
        if (definitions == null) {
            definitions = FXCollections.observableArrayList();
        }
        definitions.clear();
        definitions.add(definition);
    }

    public static ObservableList<Word> getAllWords() throws SQLException {
        ObservableList<Word> words = FXCollections.observableArrayList();
        String query = "SELECT id, term, definition FROM dictionary_words ORDER BY term";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Map<String, ObservableList<String>> wordMap = new LinkedHashMap<>();

            while (rs.next()) {
                String term = rs.getString("term");
                String def = rs.getString("definition");

                wordMap.computeIfAbsent(term, k -> FXCollections.observableArrayList()).add(def);
            }

            // Create Word objects with all definitions grouped
            for (Map.Entry<String, ObservableList<String>> entry : wordMap.entrySet()) {
                words.add(new Word(entry.getKey(), entry.getValue()));
            }
        }
        return words;
    }

    public static ObservableList<Word> searchWords(String searchTerm) throws SQLException {
        List<Word> wordsList = new ArrayList<>();
        String query = "SELECT id, term, definition FROM dictionary_words WHERE LOWER(term) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + searchTerm.toLowerCase() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                Map<String, ObservableList<String>> wordMap = new LinkedHashMap<>();

                while (rs.next()) {
                    String term = rs.getString("term");
                    String def = rs.getString("definition");

                    wordMap.computeIfAbsent(term, k -> FXCollections.observableArrayList()).add(def);
                }

                // Create Word objects with all definitions grouped
                for (Map.Entry<String, ObservableList<String>> entry : wordMap.entrySet()) {
                    wordsList.add(new Word(entry.getKey(), entry.getValue()));
                }
            }
        }

        // Sort by relevance: exact match, starts with, length, then alphabetically
        String lowerSearchTerm = searchTerm.toLowerCase();
        wordsList.sort((word1, word2) -> {
            String term1Lower = word1.getTerm().toLowerCase();
            String term2Lower = word2.getTerm().toLowerCase();

            // Priority 1: Exact match
            if (term1Lower.equals(lowerSearchTerm)) return -1;
            if (term2Lower.equals(lowerSearchTerm)) return 1;

            // Priority 2: Starts with search term
            boolean word1StartsWith = term1Lower.startsWith(lowerSearchTerm);
            boolean word2StartsWith = term2Lower.startsWith(lowerSearchTerm);
            if (word1StartsWith && !word2StartsWith) return -1;
            if (!word1StartsWith && word2StartsWith) return 1;

            // Priority 3: Shorter words first (more precise matches)
            if (term1Lower.length() != term2Lower.length()) {
                return Integer.compare(term1Lower.length(), term2Lower.length());
            }

            // Priority 4: Alphabetical order
            return term1Lower.compareTo(term2Lower);
        });

        return FXCollections.observableArrayList(wordsList);
    }

    public void save() throws SQLException {
        String query = "INSERT INTO dictionary_words (term, definition) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, this.term);
            pstmt.setString(2, this.definition);
            pstmt.executeUpdate();
        }
    }

    public void delete() throws SQLException {
        if (this.id == null) return;

        String query = "DELETE FROM dictionary_words WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setLong(1, this.id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public String toString() {
        return term;
    }
}
