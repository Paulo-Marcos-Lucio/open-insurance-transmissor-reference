# ADR 0001 — Arquitetura Hexagonal

**Status:** Aceito (v0.1.0) · **Data:** 2026-04-28

## Contexto

O Transmissor de Dados Open Insurance tem várias dependências externas potenciais: persistência (in-memory v0.1, Postgres v0.5), audit, auth (mock v0.1, FAPI-CIBA v0.2), provedor de tokens, gateway de notificação. Cada uma é candidata a ser trocada.

Sem fronteira clara, qualquer mudança quebra o domínio. E o domínio (modelos da apólice de seguro) é o ativo mais valioso e mais reutilizado.

## Decisão

Aplicar **Hexagonal Architecture (Ports & Adapters)** com 4 camadas:

- `domain/` — modelos, exceções, ports. **Apenas Java + JDK** (sem Spring, sem Jakarta)
- `application/` — use cases. Depende **apenas** de `domain/`
- `infrastructure/` — implementação dos ports out (in-memory repository, audit, seed loader)
- `adapter/web/` — entrada HTTP — controllers, DTOs, exception handler, security

Validação automática via **ArchUnit** em `HexagonalArchitectureTest`. Roda no CI; PR com violação não merge.

## Consequências

**Positivas:**
- Domain testável sem Spring (5 unit tests rodam em ms)
- Trocar in-memory por JPA na v0.5.0 não toca application/domain
- FAPI-CIBA (v0.2.0) entra como decorator/filter no adapter/web sem mexer use cases

**Negativas:**
- Mais camadas pra quem está chegando. Mitigado por ADRs e CLAUDE.md.
