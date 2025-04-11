package com.unicesumar.paymentMethods;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BoletoPayment implements PaymentMethod {
    @Override
    public void pay(double amount) {
        String codigoBoleto = generateBoletoCode();
        LocalDate dataVencimento = LocalDate.now().plusDays(3);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        System.out.println("Boleto gerado com sucesso:");
        System.out.println("Código do boleto: " + codigoBoleto);
        System.out.println("Data de vencimento: " + dataVencimento.format(formatter));
        System.out.println("Valor: R$ " + amount);
    }

    private String generateBoletoCode() {
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < 48; i++) {
            codigo.append((int)(Math.random() * 10));
            if ((i + 1) % 12 == 0 && i < 47) {
                codigo.append(" ");
            }
        }
        return codigo.toString();
    }
}