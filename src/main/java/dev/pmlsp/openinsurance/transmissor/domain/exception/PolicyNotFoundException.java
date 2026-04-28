package dev.pmlsp.openinsurance.transmissor.domain.exception;

public class PolicyNotFoundException extends RuntimeException {
    public PolicyNotFoundException(String policyId) {
        super("policy not found: " + policyId);
    }
}
