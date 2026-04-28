package dev.pmlsp.openinsurance.transmissor.infrastructure.seed;

import dev.pmlsp.openinsurance.transmissor.domain.model.Amount;
import dev.pmlsp.openinsurance.transmissor.domain.model.Coverage;
import dev.pmlsp.openinsurance.transmissor.domain.model.InsuranceCategory;
import dev.pmlsp.openinsurance.transmissor.domain.model.InsurancePolicy;
import dev.pmlsp.openinsurance.transmissor.domain.model.Insured;
import dev.pmlsp.openinsurance.transmissor.domain.model.PolicyId;
import dev.pmlsp.openinsurance.transmissor.domain.model.PolicyStatus;
import dev.pmlsp.openinsurance.transmissor.domain.model.Premium;
import dev.pmlsp.openinsurance.transmissor.domain.model.TaxId;
import dev.pmlsp.openinsurance.transmissor.domain.model.TaxId.TaxIdType;
import dev.pmlsp.openinsurance.transmissor.domain.port.out.PolicyRepository;
import dev.pmlsp.openinsurance.transmissor.infrastructure.config.OpinProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PolicySeedLoader implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PolicySeedLoader.class);

    private static final String[] SEED_TAX_IDS = {
            "12345678901", "98765432109", "11122233344",
            "55566677788", "10010010055"
    };

    private final PolicyRepository repository;
    private final OpinProperties props;

    public PolicySeedLoader(PolicyRepository repository, OpinProperties props) {
        this.repository = repository;
        this.props = props;
    }

    @Override
    public void run(String... args) {
        if (!props.seed().enabled()) {
            return;
        }
        int n = props.seed().policiesCount();
        for (int i = 0; i < n; i++) {
            repository.save(generate(i));
        }
        LOG.info("seeded {} policies into in-memory repo", n);
    }

    private InsurancePolicy generate(int seq) {
        var rnd = ThreadLocalRandom.current();
        InsuranceCategory category = InsuranceCategory.values()[seq % InsuranceCategory.values().length];
        String taxId = SEED_TAX_IDS[seq % SEED_TAX_IDS.length];
        Insured holder = new Insured(
                new TaxId(TaxIdType.CPF, taxId),
                "Segurado " + (seq + 1),
                "1985-01-01");
        LocalDate start = LocalDate.of(2026, 1, 1).plusDays(seq % 60);
        return new InsurancePolicy(
                new PolicyId(UUID.randomUUID().toString()),
                category,
                category.name() + "-PROD-" + (seq % 10 + 1),
                category.name().charAt(0) + category.name().substring(1).toLowerCase() + " Master",
                holder,
                holder,
                List.of(
                        new Coverage(category.name() + "-COV-A",
                                "Cobertura básica " + category.name(),
                                Amount.brl(String.valueOf(50000 + rnd.nextInt(450000)) + ".00")),
                        new Coverage(category.name() + "-COV-B",
                                "Cobertura adicional " + category.name(),
                                Amount.brl(String.valueOf(10000 + rnd.nextInt(40000)) + ".00"))),
                new Premium(
                        Amount.brl(String.valueOf(500 + rnd.nextInt(2000)) + ".00"),
                        rnd.nextInt(1, 13),
                        "DEBITO_CONTA"),
                start,
                start.plusYears(1),
                PolicyStatus.ACTIVE,
                props.transmissor().brandName());
    }
}
