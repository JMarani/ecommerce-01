package com.unicesumar;

import com.unicesumar.paymentMethods.*;

public class PaymentMethodFactory {

    public static PaymentMethod create(PaymentType paymentType) {
        switch (paymentType) {
            case CARTAO:
                return new CreditCardPayment();
            case BOLETO:
                return new BoletoPayment();
            case PIX:
                return new PixPayment();
            default:
                throw new IllegalArgumentException("Tipo de pagamento não suportado: " + paymentType);
        }
    }
}