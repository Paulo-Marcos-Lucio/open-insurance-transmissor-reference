package dev.pmlsp.openinsurance.transmissor.infrastructure.audit;

import dev.pmlsp.openinsurance.transmissor.domain.port.out.AuditEvent;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.AuditLog;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StructuredAuditLog implements AuditLog {

    private static final Logger LOG = LoggerFactory.getLogger("audit");
    private final MeterRegistry registry;

    public StructuredAuditLog(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void emit(AuditEvent event) {
        LOG.info("audit kind={} entity={} details={}", event.kind(), event.entityId(), event.details());
        registry.counter("opin.audit.events", "kind", event.kind().name()).increment();
    }
}
