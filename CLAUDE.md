# Guia para Claude

Notas internas para sessões futuras de desenvolvimento assistido por IA.

## O que este projeto é

Implementação Java de **referência** (não app pronto) para **Transmissor de Dados do Open Insurance Brasil** (Phase 2 — Insurance Policies). Posicionamento é portfolio público de consultoria.

Faz parte da **Suíte de Referência Regulatória BR** (5º e último repo da Suíte v1).

## Convenções inegociáveis

### Arquitetura hexagonal
- `domain/` é **puro**: sem Spring, sem Jakarta. Records imutáveis only.
- `application/` depende **apenas** de `domain/`.
- `infrastructure/` implementa portas de saída.
- `adapter/web/` é o ponto de entrada HTTP.
- `HexagonalArchitectureTest` (ArchUnit) valida tudo isso no CI.

### Records imutáveis
- TODO modelo é record. Coleções são `List.copyOf(...)` no construtor compacto.
- Validação no construtor compacto (require non-null, range, regex).

### Mascaramento de PII
- Todo CPF/CNPJ em log/audit/exception **deve** sair mascarado via `TaxId.masked()`.
- DTOs de resposta usam `taxIdMasked` em vez de `taxId` — cliente nunca vê valor cru.

### Seed loader
- `PolicySeedLoader` é `CommandLineRunner`. Popula in-memory repo no startup com `opin.seed.policies-count` apólices fake.
- 5 CPFs seed: `12345678901`, `98765432109`, `11122233344`, `55566677788`, `10010010055`.
- Distribui categorias entre as 7 do enum (AUTO, HOME, LIFE, TRAVEL, BUSINESS, HEALTH, PETS).

### Pagination padrão Susep
- Resposta tem `data` (lista) + `meta` (totalRecords, totalPages, page, pageSize) + `links` (self, first, prev, next, last).
- `prev` e `next` são null nas bordas (page=1 ou page=totalPages).
- `pageSize=0` no request usa `opin.pagination.default-page-size`. Se passar > `max-page-size`, clamp.

### Testes
- Unit: `*Test.java`. Domínio puro testado direto.
- Integration: `*IT.java`, herda `AbstractIntegrationIT` (RANDOM_PORT — não há simulator de loopback como no OF Payments).
- Architecture: `HexagonalArchitectureTest`.

### Gotchas conhecidas
- `coverages` no record `InsurancePolicy` precisa ser `List.copyOf(coverages)` no compacto, senão `coverages.add()` muta a lista interna do record.
- `OpinProperties.Mtls.bundleName` defaulta `""` se vier null no yml — record precisa permitir construção sem o campo.

## Comandos frequentes

```bash
make up      # observabilidade
make run     # app (sem mTLS)
make load    # tráfego sintético GET /policies em loop
make test    # 7 unit + 2 ArchUnit
make it      # adiciona 3 IT
```

## Operações no GitHub via gh

Mesmo padrão dos outros repos da Suíte:
```bash
export GH_TOKEN=$(printf "protocol=https\nhost=github.com\n\n" | git credential fill 2>/dev/null | sed -n 's/^password=//p' | head -1)
gh run list -R Paulo-Marcos-Lucio/open-insurance-transmissor-reference --limit 5 -w CI
```

## Branch protection

`main` protegida após primeiro push.

## Fora de escopo (não fazer sem pedido)

- Conexão real com instituição autorizada (precisa software statement Susep + ICP-Brasil)
- Persistência durável (Postgres) — hoje é só in-memory + seed
- FAPI-CIBA + DCR — roadmap v0.2.0
- Phase 3 (Resources, Customers, Product catalogs) — roadmap v0.4.0
- JWS detached signature — roadmap

## Mensagens de commit

PT-BR informal, claro, prefixo Conventional Commits.

## Memória do harness

Arquivos em `~/.claude/projects/.../memory/` documentam preferências do Paulo. Atualizar quando aprender algo durável.
