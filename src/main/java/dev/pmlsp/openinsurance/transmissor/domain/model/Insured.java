package dev.pmlsp.openinsurance.transmissor.domain.model;

import java.util.Objects;

public record Insured(TaxId taxId, String name, String birthDate) {
    public Insured {
        Objects.requireNonNull(taxId, "taxId");
        Objects.requireNonNull(name, "name");
    }

    public String maskedTaxId() {
        return taxId.masked();
    }
}
