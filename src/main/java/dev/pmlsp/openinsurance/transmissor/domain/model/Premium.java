package dev.pmlsp.openinsurance.transmissor.domain.model;

import java.util.Objects;

public record Premium(Amount totalAmount, int paymentsQuantity, String paymentMethod) {
    public Premium {
        Objects.requireNonNull(totalAmount, "totalAmount");
        Objects.requireNonNull(paymentMethod, "paymentMethod");
        if (paymentsQuantity < 1) {
            throw new IllegalArgumentException("paymentsQuantity must be >= 1");
        }
    }
}
