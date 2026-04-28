package dev.pmlsp.openinsurance.transmissor.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opin")
public record OpinProperties(
        Transmissor transmissor,
        Pagination pagination,
        Seed seed,
        Mtls mtls
) {
    public record Transmissor(String organisationId, String brandName) {}
    public record Pagination(int defaultPageSize, int maxPageSize) {}
    public record Seed(boolean enabled, int policiesCount) {}
    public record Mtls(boolean enabled, String bundleName) {
        public Mtls {
            if (bundleName == null) bundleName = "";
        }
    }
}
