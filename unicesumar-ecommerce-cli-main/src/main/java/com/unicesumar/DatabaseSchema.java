package com.unicesumar;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSchema {
    private final Connection connection;

    public DatabaseSchema(Connection connection) {
        this.connection = connection;
    }

    public void createTables() {
        createUsersTable();
        createProductsTable();
        createSalesTable();
        createSaleProductsTable();
    }

    private void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "uuid TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL" +
                ");";
        executeStatement(sql);
    }

    private void createProductsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS products (" +
                "uuid TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "price REAL NOT NULL" +
                ");";
        executeStatement(sql);
    }

    private void createSalesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS sales (" +
                "id TEXT PRIMARY KEY," +
                "user_id TEXT NOT NULL," +
                "payment_method TEXT NOT NULL," +
                "sale_date TEXT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES users(uuid)" +
                ");";
        executeStatement(sql);
    }

    private void createSaleProductsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS sale_products (" +
                "sale_id TEXT NOT NULL," +
                "product_id TEXT NOT NULL," +
                "PRIMARY KEY (sale_id, product_id)," +
                "FOREIGN KEY (sale_id) REFERENCES sales(id)," +
                "FOREIGN KEY (product_id) REFERENCES products(uuid)" +
                ");";
        executeStatement(sql);
    }

    private void executeStatement(String sql) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela: " + e.getMessage(), e);
        }
    }
}