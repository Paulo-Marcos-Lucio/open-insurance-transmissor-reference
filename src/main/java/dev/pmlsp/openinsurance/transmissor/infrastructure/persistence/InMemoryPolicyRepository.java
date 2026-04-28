package dev.pmlsp.openinsurance.transmissor.infrastructure.persistence;

import dev.pmlsp.openinsurance.transmissor.domain.model.InsurancePolicy;
import dev.pmlsp.openinsurance.transmissor.domain.model.PolicyId;
import dev.pmlsp.openinsurance.transmissor.domain.model.TaxId;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.PolicyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryPolicyRepository implements PolicyRepository {

    private final ConcurrentMap<String, InsurancePolicy> store = new ConcurrentHashMap<>();

    @Override
    public void save(InsurancePolicy policy) {
        store.put(policy.id().value(), policy);
    }

    @Override
    public Optional<InsurancePolicy> findById(PolicyId id) {
        return Optional.ofNullable(store.get(id.value()));
    }

    @Override
    public PageResult findByPolicyHolder(TaxId taxId, int page, int pageSize) {
        List<InsurancePolicy> all = store.values().stream()
                .filter(p -> p.policyHolder().taxId().equals(taxId))
                .sorted((a, b) -> a.id().value().compareTo(b.id().value()))
                .toList();
        long total = all.size();
        int totalPages = (int) Math.max(1, (total + pageSize - 1) / pageSize);
        int from = Math.min((page - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return new PageResult(all.subList(from, to), page, pageSize, total, totalPages);
    }

    @Override
    public long count() {
        return store.size();
    }
}
