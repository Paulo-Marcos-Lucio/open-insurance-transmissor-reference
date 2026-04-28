package dev.pmlsp.openinsurance.transmissor.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record InsurancePolicy(
        PolicyId id,
        InsuranceCategory category,
        String productCode,
        String productName,
        Insured policyHolder,
        Insured insured,
        List<Coverage> coverages,
        Premium premium,
        LocalDate effectiveDate,
        LocalDate expirationDate,
        PolicyStatus status,
        String brandName
) {

    public InsurancePolicy {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(productCode, "productCode");
        Objects.requireNonNull(policyHolder, "policyHolder");
        Objects.requireNonNull(coverages, "coverages");
        if (coverages.isEmpty()) {
            throw new IllegalArgumentException("policy must have at least one coverage");
        }
        Objects.requireNonNull(premium, "premium");
        Objects.requireNonNull(effectiveDate, "effectiveDate");
        Objects.requireNonNull(expirationDate, "expirationDate");
        if (expirationDate.isBefore(effectiveDate)) {
            throw new IllegalArgumentException("expirationDate must be >= effectiveDate");
        }
        Objects.requireNonNull(status, "status");
        coverages = List.copyOf(coverages);
    }

    public boolean isActive(LocalDate today) {
        return status == PolicyStatus.ACTIVE
                && !today.isBefore(effectiveDate)
                && !today.isAfter(expirationDate);
    }
}
