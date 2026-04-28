package dev.pmlsp.openinsurance.transmissor.adapter.web;

import dev.pmlsp.openinsurance.transmissor.adapter.web.dto.WebDtos.CoverageResponse;
import dev.pmlsp.openinsurance.transmissor.adapter.web.dto.WebDtos.InsuredResponse;
import dev.pmlsp.openinsurance.transmissor.adapter.web.dto.WebDtos.Links;
import dev.pmlsp.openinsurance.transmissor.adapter.web.dto.WebDtos.Meta;
import dev.pmlsp.openinsurance.transmissor.adapter.web.dto.WebDtos.PageResponse;
import dev.pmlsp.openinsurance.transmissor.adapter.web.dto.WebDtos.PolicyResponse;
import dev.pmlsp.openinsurance.transmissor.adapter.web.dto.WebDtos.PolicySummaryResponse;
import dev.pmlsp.openinsurance.transmissor.adapter.web.dto.WebDtos.PremiumResponse;
import dev.pmlsp.openinsurance.transmissor.domain.model.Coverage;
import dev.pmlsp.openinsurance.transmissor.domain.model.InsurancePolicy;
import dev.pmlsp.openinsurance.transmissor.domain.model.Insured;
import dev.pmlsp.openinsurance.transmissor.domain.model.PolicyId;
import dev.pmlsp.openinsurance.transmissor.domain.model.TaxId;
import dev.pmlsp.openinsurance.transmissor.domain.model.TaxId.TaxIdType;
import dev.pmlsp.openinsurance.transmissor.domain.port.in.GetPolicyUseCase;
import dev.pmlsp.openinsurance.transmissor.domain.port.in.ListPoliciesUseCase;
import dev.pmlsp.openinsurance.transmissor.domain.port.in.ListPoliciesUseCase.ListCommand;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.PolicyRepository.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/open-insurance/insurance-policies/v1")
public class InsurancePoliciesController {

    private final GetPolicyUseCase getPolicy;
    private final ListPoliciesUseCase listPolicies;

    public InsurancePoliciesController(GetPolicyUseCase getPolicy, ListPoliciesUseCase listPolicies) {
        this.getPolicy = getPolicy;
        this.listPolicies = listPolicies;
    }

    @GetMapping("/policies/{policyId}")
    public PolicyResponse getPolicy(@PathVariable String policyId) {
        InsurancePolicy policy = getPolicy.get(new PolicyId(policyId));
        return toFullResponse(policy);
    }

    @GetMapping("/policies")
    public PageResponse<PolicySummaryResponse> listPolicies(
            @RequestParam(name = "documentType", defaultValue = "CPF") String documentType,
            @RequestParam(name = "document") String document,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "page-size", defaultValue = "0") int pageSize) {

        TaxId taxId = new TaxId(TaxIdType.valueOf(documentType), document);
        PageResult result = listPolicies.list(new ListCommand(taxId, page, pageSize));

        var data = result.items().stream().map(p -> new PolicySummaryResponse(
                p.id().value(),
                p.category().name(),
                p.productCode(),
                p.status().name(),
                p.effectiveDate(),
                p.expirationDate())).toList();

        return new PageResponse<>(
                data,
                new Meta((int) result.totalRecords(), result.totalPages(),
                        result.page(), result.pageSize()),
                buildLinks(result, taxId, documentType));
    }

    private static PolicyResponse toFullResponse(InsurancePolicy p) {
        return new PolicyResponse(
                p.id().value(),
                p.category().name(),
                p.productCode(),
                p.productName(),
                toInsuredResponse(p.policyHolder()),
                toInsuredResponse(p.insured()),
                p.coverages().stream().map(InsurancePoliciesController::toCoverageResponse).toList(),
                new PremiumResponse(
                        p.premium().totalAmount().asString(),
                        p.premium().totalAmount().currency(),
                        p.premium().paymentsQuantity(),
                        p.premium().paymentMethod()),
                p.effectiveDate(),
                p.expirationDate(),
                p.status().name(),
                p.brandName());
    }

    private static InsuredResponse toInsuredResponse(Insured i) {
        return new InsuredResponse(i.maskedTaxId(), i.name(), i.birthDate());
    }

    private static CoverageResponse toCoverageResponse(Coverage c) {
        return new CoverageResponse(c.code(), c.description(),
                c.insuredAmount().asString(), c.insuredAmount().currency());
    }

    private static Links buildLinks(PageResult r, TaxId taxId, String documentType) {
        String base = "/open-insurance/insurance-policies/v1/policies?documentType=" + documentType
                + "&document=" + taxId.value() + "&page-size=" + r.pageSize();
        String self = base + "&page=" + r.page();
        String first = base + "&page=1";
        String last = base + "&page=" + r.totalPages();
        String prev = r.page() > 1 ? base + "&page=" + (r.page() - 1) : null;
        String next = r.page() < r.totalPages() ? base + "&page=" + (r.page() + 1) : null;
        return new Links(self, first, prev, next, last);
    }
}
