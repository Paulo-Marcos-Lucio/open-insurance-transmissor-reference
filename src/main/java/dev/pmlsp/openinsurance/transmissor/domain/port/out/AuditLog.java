package dev.pmlsp.openinsurance.transmissor.domain.port.out;

public interface AuditLog {
    void emit(AuditEvent event);
}
