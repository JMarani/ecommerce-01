package com.unicesumar.repository;

import com.unicesumar.entities.Product;
import com.unicesumar.entities.Sale;
import com.unicesumar.paymentMethods.PaymentType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SaleRepository {
    private final Connection connection;
    private final ProductRepository productRepository;

    public SaleRepository(Connection connection, ProductRepository productRepository) {
        this.connection = connection;
        this.productRepository = productRepository;
    }

    public void save(Sale sale) {
        // Primeiro, inserimos a venda
        String insertSaleQuery = "INSERT INTO sales (id, user_id, payment_method, sale_date) VALUES (?, ?, ?, ?)";
        try {
            // Desabilitar o autocommit para garantir que a transação seja atômica
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(insertSaleQuery)) {
                stmt.setString(1, sale.getUuid().toString());
                stmt.setString(2, sale.getUserId().toString());
                stmt.setString(3, sale.getPaymentMethod().toString());
                stmt.setString(4, Timestamp.valueOf(sale.getSaleDate()).toString());
                stmt.executeUpdate();
            }

            // Agora, inserimos os produtos da venda
            String insertSaleProductQuery = "INSERT INTO sale_products (sale_id, product_id) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertSaleProductQuery)) {
                for (Product product : sale.getProducts()) {
                    stmt.setString(1, sale.getUuid().toString());
                    stmt.setString(2, product.getUuid().toString());
                    stmt.executeUpdate();
                }
            }

            // Commit da transação
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Erro ao realizar rollback da transação", rollbackEx);
            }
            throw new RuntimeException("Erro ao salvar venda", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException("Erro ao restaurar autocommit", e);
            }
        }
    }

    public Optional<Sale> findById(UUID id) {
        String query = "SELECT * FROM sales WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UUID userId = UUID.fromString(rs.getString("user_id"));
                PaymentType paymentMethod = PaymentType.valueOf(rs.getString("payment_method"));
                Timestamp timestamp = rs.getTimestamp("sale_date");
                LocalDateTime saleDate = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();

                Sale sale = new Sale(id, userId, paymentMethod, saleDate);

                // Carregar os produtos da venda
                loadSaleProducts(sale);

                return Optional.of(sale);
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar venda por ID", e);
        }
    }

    private void loadSaleProducts(Sale sale) {
        String query = "SELECT product_id FROM sale_products WHERE sale_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, sale.getUuid().toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UUID productId = UUID.fromString(rs.getString("product_id"));
                Optional<Product> product = productRepository.findById(productId);
                product.ifPresent(sale::addProduct);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao carregar produtos da venda", e);
        }
    }

    public List<Sale> findAll() {
        String query = "SELECT * FROM sales";
        List<Sale> sales = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                UUID userId = UUID.fromString(rs.getString("user_id"));
                PaymentType paymentMethod = PaymentType.valueOf(rs.getString("payment_method"));
                Timestamp timestamp = rs.getTimestamp("sale_date");
                LocalDateTime saleDate = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();

                Sale sale = new Sale(id, userId, paymentMethod, saleDate);
                loadSaleProducts(sale);
                sales.add(sale);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todas as vendas", e);
        }

        return sales;
    }
}