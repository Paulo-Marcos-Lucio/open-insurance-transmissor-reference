package dev.pmlsp.openinsurance.transmissor.it;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public abstract class AbstractIntegrationIT {

    @Value("${local.server.port}")
    protected int port;

    protected RestTemplate http() {
        return new RestTemplateBuilder().build();
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }
}
