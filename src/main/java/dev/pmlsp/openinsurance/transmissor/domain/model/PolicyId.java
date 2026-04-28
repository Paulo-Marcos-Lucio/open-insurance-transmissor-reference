package dev.pmlsp.openinsurance.transmissor.domain.model;

import java.util.Objects;

public record PolicyId(String value) {
    public PolicyId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("policy id cannot be blank");
        }
    }
}
