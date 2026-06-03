package com.todolist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/todo_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "password";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String url = getEnv("DB_URL", DEFAULT_URL);
        String user = getEnv("DB_USER", DEFAULT_USER);
        String pass = getEnv("DB_PASS", DEFAULT_PASS);

        return DriverManager.getConnection(url, user, pass);
    }

    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
