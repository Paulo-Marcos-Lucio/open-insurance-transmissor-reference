[![Suíte Regulatória BR](https://img.shields.io/badge/%F0%9F%8C%90%20Su%C3%ADte%20Regulat%C3%B3ria%20BR-paulo--marcos--lucio.github.io-3fb950?style=for-the-badge)](https://paulo-marcos-lucio.github.io)

# open-insurance-transmissor-reference

> Implementação de **referência** *production-grade* do **Transmissor de Dados** do
> Open Insurance Brasil — endpoints da Phase 2 (Insurance Policies) conforme padrão
> Susep, hexagonal validada por ArchUnit, observabilidade end-to-end, mTLS-ready
> (ICP-Brasil) com roadmap explícito pra FAPI-CIBA + DCR + JWS detached em v0.2.0+.
>
> Java 21 · Spring Boot 3.4 · Hexagonal · OpenTelemetry · Grafana

[![CI](https://github.com/Paulo-Marcos-Lucio/open-insurance-transmissor-reference/actions/workflows/ci.yml/badge.svg)](https://github.com/Paulo-Marcos-Lucio/open-insurance-transmissor-reference/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.4](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)

---

## Por que este repo existe

O **Open Insurance Brasil** (OPIN) — regulado pela **Susep** — replica o modelo do Open
Finance pro mercado de seguros: o cliente autoriza o compartilhamento dos dados das suas
apólices, sinistros e produtos com terceiros. Transmissor de Dados é a seguradora ou
sociedade de capitalização que **expõe esses dados** via APIs padronizadas.

Material open source de referência em Java pro lado da Transmissora é praticamente
inexistente. Este repo cobre o **núcleo da Phase 2 (Insurance Policies)** com nível de
produção e roadmap explícito pros componentes que ainda faltam.

Faz parte da **Suíte de Referência Regulatória BR** mantida ao lado de:

- [`pix-automatico-reference`](https://github.com/Paulo-Marcos-Lucio/pix-automatico-reference) — Pix Automático + Open Finance Fase 4
- [`dict-client-reference`](https://github.com/Paulo-Marcos-Lucio/dict-client-reference) — cliente DICT do BCB
- [`pix-nfc-reference`](https://github.com/Paulo-Marcos-Lucio/pix-nfc-reference) — Pix por aproximação (NFC)
- [`open-finance-payments-reference`](https://github.com/Paulo-Marcos-Lucio/open-finance-payments-reference) — Iniciador de Pagamento (PISP) Open Finance

## O que está aqui (v0.2.0 — FAPI 2.0 + DPoP)

- **Endpoints transmissora-side (Phase 2 — Insurance Policies):**
  - `GET /open-insurance/insurance-policies/v1/policies` — lista paginada por CPF/CNPJ
  - `GET /open-insurance/insurance-policies/v1/policies/{policyId}` — detalhe completo
- **Domain:** `InsurancePolicy`, `Coverage`, `Premium`, `Insured` (com `taxId.masked()`),
  enums `InsuranceCategory` (AUTO, HOME, LIFE, TRAVEL, BUSINESS, HEALTH, PETS) e `PolicyStatus`
- **CommandLineRunner** popula 50 apólices fake no startup pra demo (configurável via
  `opin.seed.policies-count`, distribuídas entre 5 CPFs seed)
- **Pagination com meta + links** seguindo padrão Susep (`data`, `meta`, `links`)
- **PII mascarada por default** — `TaxId.masked()` retorna `12***99` em todos os retornos
- **Audit log estruturado JSON** com métricas Micrometer `opin.audit.events{kind}`
- **Observabilidade:** Prometheus + Tempo + Loki + Grafana provisionados via compose.yaml
- **mTLS ICP-Brasil-ready** via Spring Boot SSL Bundle (config-driven, off por default)
- **Hexagonal estrito + ArchUnit** — domain puro (sem Spring), application só em domain
- **CI/CD end-to-end** — 7 jobs paralelos GitHub Actions

## Quickstart

```bash
git clone https://github.com/Paulo-Marcos-Lucio/open-insurance-transmissor-reference
cd open-insurance-transmissor-reference

make up           # otel + prometheus + tempo + loki + grafana
make run          # app sobe com 50 apólices seed (cpf=12345678901, 98765432109, ...)
make load         # tráfego sintético (GET /policies em loop)
```

Acesse:
- **Swagger UI:** http://localhost:8083/swagger-ui.html
- **Grafana (admin/admin):** http://localhost:3000
- **Prometheus:** http://localhost:9090

## Endpoints (Phase 2 — Insurance Policies)

```http
GET  /open-insurance/insurance-policies/v1/policies?documentType=CPF&document=12345678901&page=1&page-size=10
GET  /open-insurance/insurance-policies/v1/policies/{policyId}
```

Veja [`requests.http`](./requests.http) pra exemplos rodáveis.

## Arquitetura

Camadas (validadas por **ArchUnit**):

- `domain/` — modelos imutáveis (records), exceções, ports. **Sem Spring, sem Jakarta**
- `application/` — use cases (`GetPolicyService`, `ListPoliciesService`). Depende **apenas** de `domain/`
- `infrastructure/` — implementação dos ports out (in-memory repository, audit, seed)
- `adapter/web/` — controllers HTTP, DTOs, exception handler, security

## Padrões de implementação destacados

| Padrão | Onde aplica | Por que importa |
|---|---|---|
| **Records imutáveis no domínio** | Todos os modelos (`InsurancePolicy`, `Coverage`, `Premium`, etc) | Thread-safe sem sincronização; testes determinísticos |
| **PII mascarada por default** | `TaxId.masked()`, `Insured.maskedTaxId()` | Logs/audit nunca expõem CPF/CNPJ completo; regulatory-friendly |
| **Pagination com meta + links** | `InsurancePoliciesController.buildLinks()` | Padrão Susep — clientes paginarem corretamente |
| **Seed loader pra demo** | `PolicySeedLoader` (`CommandLineRunner`) | Repo clonado roda com dados de exemplo, sem precisar setup |
| **Hexagonal estrito + ArchUnit** | `HexagonalArchitectureTest` | CI bloqueia regressão de fronteira |
| **DPoP RFC 9449 sender-constrained tokens** | `DPoPValidator`, `AccessTokenIntrospector`, `DPoPAuthenticationFilter` | Token roubado de outra máquina é rejeitado pelo `cnf.jkt` mismatch — segue o profile FAPI 2.0 que o OPIN herdará |

## FAPI 2.0 + DPoP (RFC 9449)

A v0.2.0 ativa um perfil `fapi` que protege os endpoints `/open-insurance/insurance-policies/**` com **DPoP sender-constrained access tokens** — o padrão que o profile Open Insurance Brasil herda do FAPI 2.0.

```bash
SPRING_PROFILES_ACTIVE=local,fapi make run
```

Wire-protocol esperado pelo receiver:

```http
POST /mock-auth/token                      # auth server in-process
DPoP: <proof JWT assinado pela chave do receiver>
X-Client-Id: demo-receiver
→ { "access_token": "...", "token_type": "DPoP", "expires_in": 600, "scope": "insurance-policies" }

GET /open-insurance/insurance-policies/v1/policies?documentType=CPF&document=...
Authorization: DPoP <access_token>         # carrega cnf.jkt = thumbprint da chave do receiver
DPoP: <proof JWT fresco pra esta requisição>
→ 200 OK + payload Phase 2
```

Validações enforced pelo `DPoPValidator`:

| Validação | Como | Falha → |
|---|---|---|
| `typ` header = `dpop+jwt` | parse JWS header | 401 `invalid_dpop_proof` |
| Algorithm em `{ES256, RS256, PS256}` | parse JWS header | 401 `invalid_dpop_proof` |
| `jwk` header presente e público | parse JWS header | 401 `invalid_dpop_proof` |
| Signature verifica sob a `jwk` embutida | nimbus-jose-jwt | 401 `invalid_dpop_proof` |
| `htm` claim bate com método HTTP (case-insensitive) | claim check | 401 `invalid_dpop_proof` |
| `htu` claim bate com URL canonicalizada (sem query/fragment) | claim check | 401 `invalid_dpop_proof` |
| `iat` dentro de ±60s | claim check | 401 `invalid_dpop_proof` |
| `jti` único no `DPoPNonceCache` (Caffeine TTL 2min) | anti-replay | 401 `invalid_dpop_proof` |
| Thumbprint da `jwk` == `cnf.jkt` do access token | binding | 401 `invalid_token` |

11 testes cobrem o validador isoladamente (8 unit em `DPoPValidatorTest`) e o flow E2E ponta-a-ponta (3 IT em `FapiE2EIT` — token issuance, request sem headers, replay attack com chave diferente).

**Não-objetivos da v0.2.0** (próximas releases):

- private_key_jwt assertion no token endpoint (RFC 7523) — v0.3.0 com FAPI-CIBA + DCR
- Fetch real do JWKS do auth server com cache TTL (hoje é in-process pra IT)
- JWS detached signature nos payloads das responses (RFC 7515 + ICP-Brasil cert)

## Configuração

Todos os parâmetros via `opin.*` em `application.yml` ou env vars correspondentes.

```yaml
opin:
  transmissor:
    organisation-id: 33333333-3333-3333-3333-333333333333
    brand-name: "Seguradora Demo S/A"
  pagination:
    default-page-size: 25
    max-page-size: 100
  seed:
    enabled: true
    policies-count: 50
  mtls:
    enabled: true
    bundle-name: opin-prod
```

## Roadmap

- [x] v0.1.0 — Phase 2 Insurance Policies (list + detail) + seed + hexagonal + observability
- [x] **v0.2.0 — FAPI 2.0 + DPoP RFC 9449** sender-constrained tokens · mock auth in-process · 11 testes
- [ ] **v0.3.0 — FAPI-CIBA + DCR (Dynamic Client Registration) + private_key_jwt** *(próximo)*
- [ ] v0.4.0 — Endpoints adicionais Phase 2 — Claim Notifications, Premium history, General info
- [ ] v0.5.0 — Phase 3 — Resources, Customers, Pets/Auto/Home product catalogs
- [ ] v0.6.0 — Persistência durável (Postgres com partitioning por categoria)
- [ ] JWS detached signature em payloads (RFC 7515 + ICP-Brasil cert)
- [ ] Conformance test contra a suite oficial do Open Insurance Brasil

## Compliance

ADRs em `docs/adr/`:

- [0001 — Hexagonal architecture](docs/adr/0001-hexagonal-architecture.md)
- [0002 — Records imutáveis no domínio](docs/adr/0002-immutable-records-domain.md)
- [0003 — Seed loader pra demo](docs/adr/0003-seed-loader-demo.md)
- [0004 — Pagination padrão Susep](docs/adr/0004-pagination-susep.md)

## Licença

[MIT](LICENSE) — use, modifique, distribua. Atribuição apreciada.

## Autor

**Paulo Marcos Lucio** — Engenheiro Java pleno · Consultor em integrações regulatórias BR

[LinkedIn](https://www.linkedin.com/in/paulo-marcos-a07379174/) ·
[GitHub](https://github.com/Paulo-Marcos-Lucio) ·
pmlsp23@gmail.com

> Se este repo ajudou seu time, ⭐ uma star — ajuda outros engenheiros do nicho a encontrarem.
