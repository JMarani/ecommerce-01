package com.unicesumar;

import com.unicesumar.entities.Product;
import com.unicesumar.entities.Sale;
import com.unicesumar.entities.User;
import com.unicesumar.paymentMethods.PaymentMethod;
import com.unicesumar.paymentMethods.PaymentType;
import com.unicesumar.repository.ProductRepository;
import com.unicesumar.repository.SaleRepository;
import com.unicesumar.repository.UserRepository;

import java.util.*;

public class SalesManager {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final PaymentManager paymentManager;

    public SalesManager(UserRepository userRepository,
                        ProductRepository productRepository,
                        SaleRepository saleRepository,
                        PaymentManager paymentManager) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.paymentManager = paymentManager;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<Product> findProductsByIds(List<UUID> productIds) {
        List<Product> products = new ArrayList<>();
        for (UUID id : productIds) {
            Optional<Product> product = productRepository.findById(id);
            product.ifPresent(products::add);
        }
        return products;
    }

    public double calculateTotal(List<Product> products) {
        return products.stream().mapToDouble(Product::getPrice).sum();
    }

    public Sale createSale(User user, List<Product> products, PaymentType paymentType) {
        Sale sale = new Sale(user.getUuid(), paymentType);

        products.forEach(sale::addProduct);

        PaymentMethod paymentMethod = PaymentMethodFactory.create(paymentType);
        paymentManager.setPaymentMethod(paymentMethod);
        paymentManager.pay(sale.getTotal());

        saleRepository.save(sale);

        return sale;
    }
}