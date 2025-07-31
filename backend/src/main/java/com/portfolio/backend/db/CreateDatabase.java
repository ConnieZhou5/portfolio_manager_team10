package com.portfolio.backend.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class CreateDatabase {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/";
        String username = "root";
        String password = "mspm123!";  // change this to match your own setup
        String dbName = "portfolio_db";

        try {
            // Connect to MySQL without specifying a DB
            Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
            Statement stmt = conn.createStatement();

            // Check if DB exists
            String checkSql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + dbName + "'";
            ResultSet rs = stmt.executeQuery(checkSql);

            if (rs.next()) {
                System.out.println("ℹ️ Database '" + dbName + "' already exists.");
            } else {
                String createSql = "CREATE DATABASE " + dbName;
                stmt.executeUpdate(createSql);
                System.out.println("✅ Database '" + dbName + "' created successfully.");
            }

            stmt.close();
            conn.close();
            System.out.println("ℹ️ Tables will be created automatically by Spring Boot when the application starts.");
        } catch (SQLException e) {
            System.out.println("❌ Error while creating database: " + e.getMessage());
        }
    }
}
