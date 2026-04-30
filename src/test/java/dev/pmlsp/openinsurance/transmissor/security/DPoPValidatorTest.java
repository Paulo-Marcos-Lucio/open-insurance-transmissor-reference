package dev.pmlsp.openinsurance.transmissor.security;

import dev.pmlsp.openinsurance.transmissor.infrastructure.security.dpop.DPoPNonceCache;
import dev.pmlsp.openinsurance.transmissor.infrastructure.security.dpop.DPoPProof;
import dev.pmlsp.openinsurance.transmissor.infrastructure.security.dpop.DPoPProofException;
import dev.pmlsp.openinsurance.transmissor.infrastructure.security.dpop.DPoPValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DPoPValidatorTest {

    private DPoPValidator validator;
    private DPoPHelper helper;

    @BeforeEach
    void setup() {
        validator = new DPoPValidator(new DPoPNonceCache());
        helper = new DPoPHelper();
    }

    @Test
    void validatesProofWithMatchingMethodAndUri() {
        String proof = helper.sign("GET", "https://api.example/v1/policies");
        DPoPProof p = validator.validate(proof, "GET",
                "https://api.example/v1/policies", helper.thumbprint());
        assertEquals("GET", p.htm());
        assertEquals(helper.thumbprint(), p.thumbprint());
    }

    @Test
    void rejectsProofWithMismatchedHtm() {
        String proof = helper.sign("GET", "https://api.example/v1/policies");
        DPoPProofException ex = assertThrows(DPoPProofException.class,
                () -> validator.validate(proof, "POST",
                        "https://api.example/v1/policies", null));
        assertTrue(ex.getMessage().contains("htm"), () -> "got: " + ex.getMessage());
    }

    @Test
    void rejectsProofWithMismatchedHtu() {
        String proof = helper.sign("GET", "https://api.example/v1/policies");
        DPoPProofException ex = assertThrows(DPoPProofException.class,
                () -> validator.validate(proof, "GET",
                        "https://api.example/v1/customers", null));
        assertTrue(ex.getMessage().contains("htu"), () -> "got: " + ex.getMessage());
    }

    @Test
    void rejectsProofWithStaleIat() {
        String proof = helper.sign("GET", "https://api.example/v1/policies",
                Instant.now().minusSeconds(120), UUID.randomUUID().toString());
        DPoPProofException ex = assertThrows(DPoPProofException.class,
                () -> validator.validate(proof, "GET",
                        "https://api.example/v1/policies", null));
        assertTrue(ex.getMessage().contains("iat"), () -> "got: " + ex.getMessage());
    }

    @Test
    void rejectsReplayedJti() {
        String jti = UUID.randomUUID().toString();
        String proof = helper.sign("GET", "https://api.example/v1/policies",
                Instant.now(), jti);
        validator.validate(proof, "GET", "https://api.example/v1/policies", null);

        DPoPProofException ex = assertThrows(DPoPProofException.class,
                () -> validator.validate(proof, "GET",
                        "https://api.example/v1/policies", null));
        assertTrue(ex.getMessage().contains("jti"), () -> "got: " + ex.getMessage());
    }

    @Test
    void rejectsThumbprintMismatch() {
        String proof = helper.sign("GET", "https://api.example/v1/policies");
        DPoPProofException ex = assertThrows(DPoPProofException.class,
                () -> validator.validate(proof, "GET",
                        "https://api.example/v1/policies", "deadbeef-not-the-real-thumbprint"));
        assertEquals("invalid_token", ex.getDpopErrorCode());
        assertTrue(ex.getMessage().contains("thumbprint"), () -> "got: " + ex.getMessage());
    }

    @Test
    void rejectsTamperedPayload() {
        String proof = helper.sign("GET", "https://api.example/v1/policies");
        // re-encode the payload with a different jti so the signature no longer matches
        String[] parts = proof.split("\\.");
        java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
        java.util.Base64.Encoder encoder = java.util.Base64.getUrlEncoder().withoutPadding();
        String original = new String(decoder.decode(parts[1]));
        String tamperedPayload = original.replaceFirst("\"jti\":\"[^\"]+\"",
                "\"jti\":\"tampered-jti-value\"");
        String tampered = parts[0] + "."
                + encoder.encodeToString(tamperedPayload.getBytes()) + "."
                + parts[2];

        DPoPProofException ex = assertThrows(DPoPProofException.class,
                () -> validator.validate(tampered, "GET",
                        "https://api.example/v1/policies", null));
        assertTrue(ex.getMessage().toLowerCase().contains("signature")
                        || ex.getMessage().toLowerCase().contains("invalid"),
                () -> "got: " + ex.getMessage());
    }

    @Test
    void canonicalizesHtuStrippingQuery() {
        String proof = helper.sign("GET", "https://api.example/v1/policies");
        DPoPProof p = validator.validate(proof, "GET",
                "https://api.example/v1/policies?documentType=CPF&document=12345678901", null);
        assertEquals("GET", p.htm());
    }
}
