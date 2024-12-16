package com.example.library;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String DATABASE_URL = "jdbc:sqlite:C:\\Users\\mixa2\\IdeaProjects\\Project\\src\\database"; // Replace with your database path
    private static final String DATABASE_USER = ""; // For SQLite, this is usually empty
    private static final String DATABASE_PASSWORD = ""; // For SQLite, this is usually empty

    // Method to get a connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
    }
}
