package dev.pmlsp.openinsurance.transmissor.domain.port.out;

import java.time.Instant;

public record AuditEvent(Instant occurredAt, AuditKind kind, String entityId, String details) {
    public enum AuditKind {
        POLICY_LOOKUP_HIT,
        POLICY_LOOKUP_MISS,
        POLICY_LIST_REQUESTED,
        POLICY_SAVED
    }
}
