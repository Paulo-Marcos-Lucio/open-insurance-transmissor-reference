package dev.pmlsp.openinsurance.transmissor.application.policy;

import dev.pmlsp.openinsurance.transmissor.domain.exception.PolicyNotFoundException;
import dev.pmlsp.openinsurance.transmissor.domain.model.InsurancePolicy;
import dev.pmlsp.openinsurance.transmissor.domain.model.PolicyId;
import dev.pmlsp.openinsurance.transmissor.domain.port.in.GetPolicyUseCase;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.AuditEvent;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.AuditEvent.AuditKind;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.AuditLog;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.PolicyRepository;

import java.time.Instant;

public class GetPolicyService implements GetPolicyUseCase {

    private final PolicyRepository repository;
    private final AuditLog auditLog;

    public GetPolicyService(PolicyRepository repository, AuditLog auditLog) {
        this.repository = repository;
        this.auditLog = auditLog;
    }

    @Override
    public InsurancePolicy get(PolicyId id) {
        return repository.findById(id)
                .map(p -> {
                    auditLog.emit(new AuditEvent(Instant.now(), AuditKind.POLICY_LOOKUP_HIT,
                            p.id().value(), "category=" + p.category()));
                    return p;
                })
                .orElseThrow(() -> {
                    auditLog.emit(new AuditEvent(Instant.now(), AuditKind.POLICY_LOOKUP_MISS,
                            id.value(), null));
                    return new PolicyNotFoundException(id.value());
                });
    }
}
