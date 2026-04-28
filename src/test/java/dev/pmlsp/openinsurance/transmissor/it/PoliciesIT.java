package dev.pmlsp.openinsurance.transmissor.it;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(properties = {
        "opin.seed.enabled=true",
        "opin.seed.policies-count=20"
})
class PoliciesIT extends AbstractIntegrationIT {

    @Test
    void listsPoliciesByPolicyHolderTaxId() {
        ResponseEntity<Map> response = http().getForEntity(
                baseUrl() + "/open-insurance/insurance-policies/v1/policies?documentType=CPF&document=12345678901&page-size=5",
                Map.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map body = response.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> meta = (Map<String, Object>) body.get("meta");

        assertNotNull(data);
        assertNotNull(meta);
        assertTrue(((Number) meta.get("totalRecords")).intValue() > 0);
        assertEquals(5, ((Number) meta.get("pageSize")).intValue());
        assertEquals(1, ((Number) meta.get("page")).intValue());
        assertTrue(data.size() <= 5);
        if (!data.isEmpty()) {
            assertNotNull(data.get(0).get("policyId"));
            assertNotNull(data.get(0).get("category"));
        }
    }

    @Test
    void getPolicyByIdReturnsFullDetail() {
        ResponseEntity<Map> list = http().getForEntity(
                baseUrl() + "/open-insurance/insurance-policies/v1/policies?documentType=CPF&document=12345678901&page-size=1",
                Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) list.getBody().get("data");
        assertTrue(!data.isEmpty(), "expected at least one seeded policy for that CPF");
        String policyId = (String) data.get(0).get("policyId");

        ResponseEntity<Map> detail = http().getForEntity(
                baseUrl() + "/open-insurance/insurance-policies/v1/policies/" + policyId,
                Map.class);
        assertTrue(detail.getStatusCode().is2xxSuccessful());
        Map body = detail.getBody();
        assertEquals(policyId, body.get("policyId"));
        assertNotNull(body.get("category"));
        assertNotNull(body.get("coverages"));
        assertNotNull(body.get("premium"));
        @SuppressWarnings("unchecked")
        Map<String, Object> insured = (Map<String, Object>) body.get("insured");
        assertNotNull(insured);
        String maskedTaxId = (String) insured.get("taxIdMasked");
        assertTrue(maskedTaxId.contains("***"), "tax id should be masked, got " + maskedTaxId);
    }

    @Test
    void getMissingPolicyReturns404() {
        try {
            http().getForEntity(
                    baseUrl() + "/open-insurance/insurance-policies/v1/policies/" + "does-not-exist",
                    Map.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertEquals(404, e.getStatusCode().value());
            return;
        }
        throw new AssertionError("expected 404");
    }
}
