package com.unicesumar.paymentMethods;

public class PixPayment implements PaymentMethod {
    @Override
    public void pay(double amount) {
        String chaveAutenticacao = generateAuthKey();
        System.out.println("Pagamento confirmado com sucesso via PIX. Chave de Autenticação: " + chaveAutenticacao);
    }

    private String generateAuthKey() {
        return String.format("%08X-%04X-%04X-%04X-%012X",
                (int)(Math.random() * 0xFFFFFFFF),
                (int)(Math.random() * 0xFFFF),
                (int)(Math.random() * 0xFFFF),
                (int)(Math.random() * 0xFFFF),
                (long)(Math.random() * 0xFFFFFFFFFFFFl));
    }
}