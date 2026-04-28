package dev.pmlsp.openinsurance.transmissor.domain.port.in;

import dev.pmlsp.openinsurance.transmissor.domain.model.TaxId;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.PolicyRepository.PageResult;

public interface ListPoliciesUseCase {
    PageResult list(ListCommand command);

    record ListCommand(TaxId policyHolderTaxId, int page, int pageSize) {}
}
