package dev.pmlsp.openinsurance.transmissor.domain.model;

import java.util.Objects;

public record Coverage(String code, String description, Amount insuredAmount) {
    public Coverage {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(insuredAmount, "insuredAmount");
    }
}
