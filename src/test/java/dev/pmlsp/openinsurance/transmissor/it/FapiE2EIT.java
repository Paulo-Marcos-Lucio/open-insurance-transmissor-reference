package dev.pmlsp.openinsurance.transmissor.it;

import dev.pmlsp.openinsurance.transmissor.security.DPoPHelper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end FAPI flow against the live Spring context with profile {@code fapi} active:
 * client requests a sender-constrained access token from the in-process mock auth
 * server, then uses it (with a fresh DPoP proof) to read protected Insurance Policies.
 * <p>
 * Exercises the same wire shape an Open Insurance Brasil receiver would use against
 * a transmissor enforcing FAPI 2.0 + DPoP RFC 9449.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"local", "fapi"})
class FapiE2EIT {

    @LocalServerPort
    int port;

    private RestTemplate http() {
        return new RestTemplateBuilder().build();
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private String policiesUrl() {
        return baseUrl() + "/open-insurance/insurance-policies/v1/policies"
                + "?documentType=CPF&document=12345678901";
    }

    @Test
    void receiverCanListPoliciesWithDpopBoundToken() {
        DPoPHelper dpop = new DPoPHelper();

        // 1) Receiver requests access token from mock auth, presenting DPoP proof
        String tokenUrl = baseUrl() + "/mock-auth/token";
        String tokenProof = dpop.sign("POST", tokenUrl);

        HttpHeaders tokenReqHeaders = new HttpHeaders();
        tokenReqHeaders.set("DPoP", tokenProof);
        tokenReqHeaders.set("X-Client-Id", "demo-receiver");

        ResponseEntity<Map> tokenResponse = http().exchange(tokenUrl, HttpMethod.POST,
                new HttpEntity<>(null, tokenReqHeaders), Map.class);
        assertTrue(tokenResponse.getStatusCode().is2xxSuccessful());
        @SuppressWarnings("unchecked")
        Map<String, Object> tokenBody = tokenResponse.getBody();
        assertNotNull(tokenBody);
        String accessToken = (String) tokenBody.get("access_token");
        assertNotNull(accessToken);
        assertEquals("DPoP", tokenBody.get("token_type"));

        // 2) Receiver uses the bound token to fetch insurance policies
        // htu must canonicalize to the URL without query (the validator strips ?...)
        String resourceUrl = policiesUrl();
        String resourceHtu = baseUrl() + "/open-insurance/insurance-policies/v1/policies";
        String resourceProof = dpop.sign("GET", resourceHtu);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "DPoP " + accessToken);
        headers.set("DPoP", resourceProof);

        ResponseEntity<Map> resp = http().exchange(resourceUrl, HttpMethod.GET,
                new HttpEntity<>(null, headers), Map.class);
        assertTrue(resp.getStatusCode().is2xxSuccessful(),
                () -> "expected 2xx, got " + resp.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = resp.getBody();
        assertNotNull(body);
        assertNotNull(body.get("data"), "response missing 'data' field");
        assertNotNull(body.get("meta"), "response missing 'meta' field");
    }

    @Test
    void requestWithoutDpopHeadersIsRejected() {
        try {
            http().exchange(policiesUrl(), HttpMethod.GET,
                    new HttpEntity<>(null, new HttpHeaders()), Map.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            assertNotNull(e.getResponseHeaders().getFirst("WWW-Authenticate"));
            return;
        }
        throw new AssertionError("expected 401");
    }

    @Test
    void replayedTokenFromAnotherKeyIsRejected() {
        DPoPHelper attacker = new DPoPHelper();
        DPoPHelper victim = new DPoPHelper();

        // Victim legitimately obtains a token bound to their key
        String tokenUrl = baseUrl() + "/mock-auth/token";
        HttpHeaders th = new HttpHeaders();
        th.set("DPoP", victim.sign("POST", tokenUrl));
        ResponseEntity<Map> r = http().exchange(tokenUrl, HttpMethod.POST,
                new HttpEntity<>(null, th), Map.class);
        @SuppressWarnings("unchecked")
        String stolenToken = (String) r.getBody().get("access_token");
        assertNotNull(stolenToken);

        // Attacker tries to use the stolen token with their own DPoP proof
        String resourceHtu = baseUrl() + "/open-insurance/insurance-policies/v1/policies";
        HttpHeaders attackHeaders = new HttpHeaders();
        attackHeaders.set("Authorization", "DPoP " + stolenToken);
        attackHeaders.set("DPoP", attacker.sign("GET", resourceHtu));

        try {
            http().exchange(policiesUrl(), HttpMethod.GET,
                    new HttpEntity<>(null, attackHeaders), Map.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            String www = e.getResponseHeaders().getFirst("WWW-Authenticate");
            assertNotNull(www);
            assertTrue(www.contains("invalid_token"), () -> "got: " + www);
            return;
        }
        throw new AssertionError("expected 401 due to thumbprint mismatch");
    }
}
