package dev.pmlsp.openinsurance.transmissor.domain.model;

import java.util.Objects;

public record TaxId(TaxIdType type, String value) {
    public TaxId {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(value, "value");
        switch (type) {
            case CPF -> {
                if (value.length() != 11) {
                    throw new IllegalArgumentException("CPF must have 11 digits, got " + value.length());
                }
            }
            case CNPJ -> {
                if (value.length() != 14) {
                    throw new IllegalArgumentException("CNPJ must have 14 digits, got " + value.length());
                }
            }
        }
    }

    public String masked() {
        if (value.length() <= 4) {
            return "***";
        }
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }

    public enum TaxIdType { CPF, CNPJ }
}
