package dev.pmlsp.openinsurance.transmissor.domain.port.in;

import dev.pmlsp.openinsurance.transmissor.domain.model.InsurancePolicy;
import dev.pmlsp.openinsurance.transmissor.domain.model.PolicyId;

public interface GetPolicyUseCase {
    InsurancePolicy get(PolicyId id);
}
