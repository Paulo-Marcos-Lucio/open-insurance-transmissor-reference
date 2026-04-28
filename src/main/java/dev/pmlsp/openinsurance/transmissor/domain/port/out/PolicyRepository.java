package dev.pmlsp.openinsurance.transmissor.domain.port.out;

import dev.pmlsp.openinsurance.transmissor.domain.model.InsurancePolicy;
import dev.pmlsp.openinsurance.transmissor.domain.model.PolicyId;
import dev.pmlsp.openinsurance.transmissor.domain.model.TaxId;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository {
    void save(InsurancePolicy policy);
    Optional<InsurancePolicy> findById(PolicyId id);
    PageResult findByPolicyHolder(TaxId taxId, int page, int pageSize);
    long count();

    record PageResult(List<InsurancePolicy> items, int page, int pageSize, long totalRecords, int totalPages) {}
}
