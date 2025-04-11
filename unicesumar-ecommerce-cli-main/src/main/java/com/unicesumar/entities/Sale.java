package com.unicesumar.entities;

import com.unicesumar.paymentMethods.PaymentType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Sale extends Entity {
    private UUID userId;
    private PaymentType paymentMethod;
    private LocalDateTime saleDate;
    private List<Product> products;

    public Sale(UUID userId, PaymentType paymentMethod) {
        super();
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.saleDate = LocalDateTime.now();
        this.products = new ArrayList<>();
    }

    public Sale(UUID id, UUID userId, PaymentType paymentMethod, LocalDateTime saleDate) {
        super(id);
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.saleDate = saleDate;
        this.products = new ArrayList<>();
    }

    public UUID getUserId() {
        return userId;
    }

    public PaymentType getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public double getTotal() {
        return products.stream().mapToDouble(Product::getPrice).sum();
    }
}