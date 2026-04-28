package dev.pmlsp.openinsurance.transmissor.adapter.web.dto;

import java.time.LocalDate;
import java.util.List;

public final class WebDtos {

    private WebDtos() {}

    public record InsuredResponse(String taxIdMasked, String name, String birthDate) {}

    public record CoverageResponse(String code, String description, String insuredAmount, String currency) {}

    public record PremiumResponse(String totalAmount, String currency, int paymentsQuantity, String paymentMethod) {}

    public record PolicyResponse(
            String policyId,
            String category,
            String productCode,
            String productName,
            InsuredResponse policyHolder,
            InsuredResponse insured,
            List<CoverageResponse> coverages,
            PremiumResponse premium,
            LocalDate effectiveDate,
            LocalDate expirationDate,
            String status,
            String brandName
    ) {}

    public record PolicySummaryResponse(
            String policyId,
            String category,
            String productCode,
            String status,
            LocalDate effectiveDate,
            LocalDate expirationDate
    ) {}

    public record PageResponse<T>(
            List<T> data,
            Meta meta,
            Links links
    ) {}

    public record Meta(int totalRecords, int totalPages, int page, int pageSize) {}

    public record Links(String self, String first, String prev, String next, String last) {}

    public record ErrorResponse(String code, String message, String requestId) {}
}
