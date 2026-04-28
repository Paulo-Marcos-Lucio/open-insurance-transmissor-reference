# Changelog

Todas as mudanças relevantes deste projeto são documentadas neste arquivo.

O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o versionamento segue [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [Unreleased]

## [0.1.0] - 2026-04-28

Primeira release pública. Implementação Java de referência para **Transmissor de Dados do Open Insurance Brasil — Phase 2 (Insurance Policies)**.

### Added — Domínio

- `InsurancePolicy` record com `coverages` imutável (`List.copyOf`), validação `expirationDate >= effectiveDate`, helper `isActive(today)`
- `Coverage`, `Premium`, `Insured` records com validação no compacto
- `TaxId` (CPF/CNPJ) com `masked()` retornando `12***99`
- `Amount` (BRL com 2 decimais, ISO 4217 currency code validado)
- Enums: `InsuranceCategory` (AUTO, HOME, LIFE, TRAVEL, BUSINESS, HEALTH, PETS) e `PolicyStatus` (ACTIVE, EXPIRED, CANCELLED, SUSPENDED)

### Added — Use cases

- `GetPolicyService` — leitura por ID com audit (`POLICY_LOOKUP_HIT|MISS`)
- `ListPoliciesService` — paginação com clamp ao `max-page-size`, audit `POLICY_LIST_REQUESTED`

### Added — Infrastructure

- `InMemoryPolicyRepository` com `ConcurrentHashMap` + filtragem por taxId do policyHolder
- `PolicySeedLoader` (`CommandLineRunner`) popula 50 apólices fake distribuídas entre 5 CPFs e 7 categorias
- `StructuredAuditLog` emitindo log JSON + `opin.audit.events{kind}` Micrometer counter

### Added — Web facade

- `GET /open-insurance/insurance-policies/v1/policies?documentType=&document=&page=&page-size=` — paginated, padrão Susep com `data` + `meta` + `links`
- `GET /open-insurance/insurance-policies/v1/policies/{policyId}` — full detail com taxId mascarado
- `GlobalExceptionHandler` mapeando `PolicyNotFoundException` → 404, `IllegalArgumentException` → 400
- Mock auth (Spring Security `permitAll`) — FAPI-CIBA é roadmap v0.2.0

### Added — Observabilidade

- Métricas via `opin.audit.events{kind}` (counters)
- Spring/Micrometer auto-emitidos: `http_server_requests_seconds_*` com **histogram buckets habilitados** via `management.metrics.distribution.percentiles-histogram.http.server.requests: true`
- Prometheus + Tempo + Loki + Grafana provisionados via compose.yaml
- 1 dashboard `OF Insurance · Operations Overview` (provisionado, com queries que aproveitam histogram bucket)

### Added — Qualidade & CI

- 5 unit tests (`InsurancePolicyTest`) + 2 ArchUnit tests + 3 IT (`PoliciesIT`)
- ArchUnit valida hexagonal estrita (domain sem Spring/Jakarta, application só em domain)
- 7 jobs CI paralelos

### Documentação

- 4 ADRs (`docs/adr/`): hexagonal, immutable records, seed loader, pagination padrão Susep
- `requests.http` com cenários básicos pra IntelliJ/VS Code REST Client

### Roadmap explícito (não nesta release)

- v0.2.0 — FAPI-CIBA + DCR (Dynamic Client Registration)
- v0.3.0 — Endpoints adicionais Phase 2 (Claim Notifications, Premium history, General info)
- v0.4.0 — Phase 3 (Resources, Customers, Product catalogs)
- v0.5.0 — Persistência durável (Postgres com partitioning por categoria)
- JWS detached signature em payloads
- Conformance test contra suite oficial Susep

[Unreleased]: https://github.com/Paulo-Marcos-Lucio/open-insurance-transmissor-reference/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/Paulo-Marcos-Lucio/open-insurance-transmissor-reference/releases/tag/v0.1.0
