# ADR 0002 — Records imutáveis no domínio

**Status:** Aceito (v0.1.0) · **Data:** 2026-04-28

## Contexto

Modelos de apólice de seguro são naturalmente imutáveis — uma apólice emitida não muta seu valor; mudanças geram nova apólice ou novo evento (claim, endosso). Tradicionalmente Java implementa isso como classes com campos `final` + Lombok `@Value`. Mas Java 17+ tem records nativamente, com sintaxe melhor e compatibilidade com pattern matching futuro.

## Decisão

**Todo modelo de domínio é record.** Coleções são `List.copyOf(...)` no construtor compacto pra prevenir mutação externa. Validação acontece no compacto (require non-null, range, regex).

Exemplo `InsurancePolicy`:

```java
public record InsurancePolicy(
        PolicyId id, InsuranceCategory category, String productCode,
        Insured policyHolder, List<Coverage> coverages, /* ... */) {

    public InsurancePolicy {
        Objects.requireNonNull(id, "id");
        if (coverages.isEmpty()) {
            throw new IllegalArgumentException("policy must have at least one coverage");
        }
        if (expirationDate.isBefore(effectiveDate)) {
            throw new IllegalArgumentException("expirationDate must be >= effectiveDate");
        }
        coverages = List.copyOf(coverages);  // imutável + cópia defensiva
    }
}
```

## Consequências

**Positivas:**
- Thread-safe sem sincronização — pode passar entre threads e cachear sem cuidado
- Testes determinísticos (`equals` baseado em valor)
- Pattern matching ready (Java 21)
- Sem Lombok no domain — uma dependência a menos

**Negativas:**
- Sem builders nativos — pra modelos com 10+ fields fica verboso. Considerar `static factory methods` quando fizer sentido (ver `EndToEndId.generate()` no `open-finance-payments-reference`)
- Mutação requer reconstrução completa — esperado pra records

## Gotcha

`List.copyOf` é necessário no compacto. Sem ele, `policy.coverages().add(c)` muta a coleção interna do record. Test `coveragesListIsImmutable` cobre essa regressão.
