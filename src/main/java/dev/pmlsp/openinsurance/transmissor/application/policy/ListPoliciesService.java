package dev.pmlsp.openinsurance.transmissor.application.policy;

import dev.pmlsp.openinsurance.transmissor.domain.port.in.ListPoliciesUseCase;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.AuditEvent;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.AuditEvent.AuditKind;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.AuditLog;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.PolicyRepository;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.PolicyRepository.PageResult;

import java.time.Instant;

public class ListPoliciesService implements ListPoliciesUseCase {

    private final PolicyRepository repository;
    private final AuditLog auditLog;
    private final int defaultPageSize;
    private final int maxPageSize;

    public ListPoliciesService(PolicyRepository repository, AuditLog auditLog,
                               int defaultPageSize, int maxPageSize) {
        this.repository = repository;
        this.auditLog = auditLog;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
    }

    @Override
    public PageResult list(ListCommand command) {
        int page = Math.max(1, command.page());
        int requested = command.pageSize() > 0 ? command.pageSize() : defaultPageSize;
        int pageSize = Math.min(requested, maxPageSize);

        PageResult result = repository.findByPolicyHolder(command.policyHolderTaxId(), page, pageSize);
        auditLog.emit(new AuditEvent(Instant.now(), AuditKind.POLICY_LIST_REQUESTED,
                command.policyHolderTaxId().masked(),
                "page=" + page + " size=" + pageSize + " total=" + result.totalRecords()));
        return result;
    }
}
