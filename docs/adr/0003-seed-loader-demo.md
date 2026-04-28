# ADR 0003 — Seed loader pra demo

**Status:** Aceito (v0.1.0) · **Data:** 2026-04-28

## Contexto

Repo de referência precisa rodar zero-friction no `git clone && make run`. Sem dados, os endpoints retornam vazio — ruim pra demo, ruim pro vídeo de launch, ruim pra screenshot de Grafana.

Opções:
1. **SQL fixture** carregado em startup — exige Postgres ou H2 + scripts
2. **JSON fixture** lido por classpath — exige parser + mapeamento
3. **CommandLineRunner** populando in-memory repo direto — código Java puro, controlado por property

## Decisão

`PolicySeedLoader` é `CommandLineRunner` que popula `InMemoryPolicyRepository` no startup. Configurável:

```yaml
opin:
  seed:
    enabled: true
    policies-count: 50
```

Distribui entre **5 CPFs seed** fixos e **7 categorias** (AUTO, HOME, LIFE, TRAVEL, BUSINESS, HEALTH, PETS) round-robin. Valores aleatórios pra premium (R$500-R$2500) e coverage insured amount (R$50k-R$500k) — `ThreadLocalRandom` pra ser determinístico-ish dentro da sessão.

Em produção real (com Postgres), `enabled: false` desabilita. Seed loader nunca toca dados existentes.

## Consequências

**Positivas:**
- `git clone && make run && curl /policies?...12345678901` retorna 10 apólices imediatamente
- Vídeo demo + dashboard popula em segundos sem setup manual
- Easy override via env var (`OPIN_SEED_POLICIES_COUNT=500` pra load test)

**Negativas:**
- Acoplamento explícito entre infrastructure e domain (loader importa modelos do domain) — esperado, é responsabilidade de infra
- Não substitui dados reais em produção — `enabled: false` em prod

## Gotcha

`PolicySeedLoader` precisa rodar **depois** do repository ser injetado. Como é `CommandLineRunner`, isso é automático no Spring Boot startup lifecycle.
