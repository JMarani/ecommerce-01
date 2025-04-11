package com.unicesumar.paymentMethods;

public class CreditCardPayment implements PaymentMethod {
    @Override
    public void pay(double amount) {
        String transactionId = generateTransactionId();
        System.out.println("Pagamento confirmado com sucesso via Cartão de Crédito. ID da Transação: " + transactionId);
    }

    private String generateTransactionId() {
        return String.format("CC-%09d", (int)(Math.random() * 1000000000));
    }
}