package dev.pmlsp.openinsurance.transmissor.infrastructure.config;

import dev.pmlsp.openinsurance.transmissor.application.policy.GetPolicyService;
import dev.pmlsp.openinsurance.transmissor.application.policy.ListPoliciesService;
import dev.pmlsp.openinsurance.transmissor.domain.port.in.GetPolicyUseCase;
import dev.pmlsp.openinsurance.transmissor.domain.port.in.ListPoliciesUseCase;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.AuditLog;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.PolicyRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpinAutoConfig {

    @Bean
    public GetPolicyUseCase getPolicyUseCase(PolicyRepository repository, AuditLog auditLog) {
        return new GetPolicyService(repository, auditLog);
    }

    @Bean
    public ListPoliciesUseCase listPoliciesUseCase(PolicyRepository repository,
                                                   AuditLog auditLog,
                                                   OpinProperties props) {
        return new ListPoliciesService(repository, auditLog,
                props.pagination().defaultPageSize(),
                props.pagination().maxPageSize());
    }
}
