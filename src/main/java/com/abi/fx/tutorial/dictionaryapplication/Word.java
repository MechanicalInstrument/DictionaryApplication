package com.abi.fx.tutorial.dictionaryapplication;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class Word {
    private Long id;
    private String term;
    private String definition;

    public Word(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }

    public Word(Long id, String term, String definition) {
        this.id = id;
        this.term = term;
        this.definition = definition;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public static ObservableList<Word> getAllWords() throws SQLException {
        ObservableList<Word> words = FXCollections.observableArrayList();
        String query = "SELECT id, term, definition FROM dictionary_words ORDER BY term";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                words.add(new Word(
                        rs.getLong("id"),
                        rs.getString("term"),
                        rs.getString("definition")
                ));
            }
        }
        return words;
    }

    public static ObservableList<Word> searchWords(String searchTerm) throws SQLException {
        ObservableList<Word> words = FXCollections.observableArrayList();
        String query = "SELECT id, term, definition FROM dictionary_words WHERE LOWER(term) LIKE ? ORDER BY term";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + searchTerm.toLowerCase() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    words.add(new Word(
                            rs.getLong("id"),
                            rs.getString("term"),
                            rs.getString("definition")
                    ));
                }
            }
        }
        return words;
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
