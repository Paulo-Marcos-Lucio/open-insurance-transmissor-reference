# ADR 0004 — Pagination padrão Susep

**Status:** Aceito (v0.1.0) · **Data:** 2026-04-28

## Contexto

A spec do Open Insurance Brasil herda o padrão de paginação do Open Finance Brasil — três campos top-level no JSON de resposta:

- `data` — array de itens da página
- `meta` — totalRecords, totalPages, page, pageSize
- `links` — self, first, prev, next, last (cada um URL ou `null` nas bordas)

Isso é diferente de:
- HAL (`_embedded` + `_links`)
- JSON:API (`data` + `links` + `meta` mas estruturas diferentes)
- Cursor-based (`before/after`/`next-cursor`)

Cliente Susep espera **exatamente** esse formato. Desviar quebra interoperabilidade.

## Decisão

`InsurancePoliciesController.listPolicies()` retorna `PageResponse<T>` com os 3 campos:

```java
public record PageResponse<T>(List<T> data, Meta meta, Links links) {}
public record Meta(int totalRecords, int totalPages, int page, int pageSize) {}
public record Links(String self, String first, String prev, String next, String last) {}
```

`buildLinks(...)` constroi cada URL preservando os query params do request original. `prev=null` quando `page=1`; `next=null` quando `page=totalPages`.

`pageSize=0` no request usa `opin.pagination.default-page-size` (25). Se cliente passar valor > `max-page-size` (100), clamp ao máximo — `ListPoliciesService.list()` faz isso.

## Consequências

**Positivas:**
- Interop direto com clientes Susep
- Cliente percorre via `links.next` sem precisar manter estado
- `meta.totalPages` permite skip-to-last UI

**Negativas:**
- Mais ceremônia que cursor-based pra datasets enormes (cursor é mais eficiente em DB)
- Requer `count(*)` na query — pode ser caro em Postgres com milhões de rows. Considerar approximate count ou materialização em v0.x
