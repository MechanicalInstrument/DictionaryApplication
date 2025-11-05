package com.abi.fx.tutorial.dictionaryapplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The amount of pointless headache and hate the oracle DB has created in me is indescribbel.<br>
 * Like Why Cant you just be normal like MYSQL, Why do you have be so insufferable.
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:FREE";
    private static final String USER = "DICTIONARYUSER";
    private static final String PASSWORD = "Some467Gwu2";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create the dictionary_words table if it doesn't exist
            String createTableSQL = """
                        CREATE TABLE dictionary_words (
                            id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            term VARCHAR2(255) NOT NULL,
                            definition CLOB NOT NULL
                        )
                    """;

            try {
                stmt.execute(createTableSQL);
                System.out.println("Dictionary table created successfully");
            } catch (SQLException e) {
                // Table might already exist, which is fine
                if (e.getErrorCode() != 955) { // ORA-00955: name is already used by an existing object
                    throw e;
                }
            }

            // Create an index on the term column for faster searches
            try {
                stmt.execute("CREATE INDEX idx_term ON dictionary_words(LOWER(term))");
                System.out.println("Index created successfully");
            } catch (SQLException e) {
                // Index might already exist, which is fine
                if (e.getErrorCode() != 955) {
                    throw e;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
