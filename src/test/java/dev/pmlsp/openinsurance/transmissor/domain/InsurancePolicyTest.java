package dev.pmlsp.openinsurance.transmissor.domain;

import dev.pmlsp.openinsurance.transmissor.domain.model.Amount;
import dev.pmlsp.openinsurance.transmissor.domain.model.Coverage;
import dev.pmlsp.openinsurance.transmissor.domain.model.InsuranceCategory;
import dev.pmlsp.openinsurance.transmissor.domain.model.InsurancePolicy;
import dev.pmlsp.openinsurance.transmissor.domain.model.Insured;
import dev.pmlsp.openinsurance.transmissor.domain.model.PolicyId;
import dev.pmlsp.openinsurance.transmissor.domain.model.PolicyStatus;
import dev.pmlsp.openinsurance.transmissor.domain.model.Premium;
import dev.pmlsp.openinsurance.transmissor.domain.model.TaxId;
import dev.pmlsp.openinsurance.transmissor.domain.model.TaxId.TaxIdType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InsurancePolicyTest {

    private final Insured holder = new Insured(
            new TaxId(TaxIdType.CPF, "12345678901"), "Fulano", "1985-01-01");
    private final Coverage coverage = new Coverage("AUTO-COV-A",
            "Cobertura básica auto", Amount.brl("100000.00"));
    private final Premium premium = new Premium(Amount.brl("1200.00"), 12, "DEBITO_CONTA");

    @Test
    void buildsActivePolicy() {
        InsurancePolicy p = new InsurancePolicy(
                new PolicyId("p1"), InsuranceCategory.AUTO,
                "AUTO-PROD-1", "Auto Master",
                holder, holder, List.of(coverage), premium,
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1),
                PolicyStatus.ACTIVE, "Seguradora Demo");
        assertTrue(p.isActive(LocalDate.of(2026, 6, 15)));
        assertFalse(p.isActive(LocalDate.of(2025, 12, 31)));
        assertFalse(p.isActive(LocalDate.of(2027, 6, 1)));
    }

    @Test
    void cannotHaveZeroCoverages() {
        assertThrows(IllegalArgumentException.class, () -> new InsurancePolicy(
                new PolicyId("p1"), InsuranceCategory.AUTO,
                "AUTO-PROD-1", "Auto Master",
                holder, holder, List.of(), premium,
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1),
                PolicyStatus.ACTIVE, "Seguradora"));
    }

    @Test
    void expirationMustBeAfterEffective() {
        assertThrows(IllegalArgumentException.class, () -> new InsurancePolicy(
                new PolicyId("p1"), InsuranceCategory.AUTO,
                "AUTO-PROD-1", "Auto Master",
                holder, holder, List.of(coverage), premium,
                LocalDate.of(2027, 1, 1), LocalDate.of(2026, 1, 1),
                PolicyStatus.ACTIVE, "Seguradora"));
    }

    @Test
    void taxIdMaskedHidesMiddleDigits() {
        TaxId t = new TaxId(TaxIdType.CPF, "12345678901");
        assertEquals("12***01", t.masked());
    }

    @Test
    void coveragesListIsImmutable() {
        InsurancePolicy p = new InsurancePolicy(
                new PolicyId("p1"), InsuranceCategory.AUTO,
                "AUTO-PROD-1", "Auto Master",
                holder, holder, List.of(coverage), premium,
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1),
                PolicyStatus.ACTIVE, "Seguradora");
        assertThrows(UnsupportedOperationException.class,
                () -> p.coverages().add(coverage));
    }
}
