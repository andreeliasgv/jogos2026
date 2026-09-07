# Jogos 2026

API desenvolvida durante a disciplina de Programação.

## Tema

API para controle de jogos de uma loja, organizados por gênero.

## Modelo de domínio

```text
GeneroJogo     1 ─────── 0..N Jogo
Desenvolvedora 0..1 ──── 0..N Jogo
```

## Persistência

A aplicação utiliza Spring Data JPA, PostgreSQL e Liquibase. Os ambientes
de desenvolvimento e testes usam bancos separados, e o Hibernate apenas
valida o esquema criado pelas migrações.

Na Aula 06, o modelo foi evoluído com a entidade `Desenvolvedora` e o
campo `estoqueMinimo`. A migration foi gerada de forma assistida por
Liquibase Diff, revisada manualmente e aplicada preservando os dados
existentes.

## Camada de aplicação

Repositories Spring Data JPA realizam o acesso aos dados. Serviços
transacionais coordenam os casos de uso e preservam os comportamentos
definidos no domínio.

## Entregas

- [Aula 03 — Modelo de domínio](docs/03aula/entrega.md)
- [Aula 04 — Persistência](docs/04aula/entrega.md)
- [Aula 05 — Repositories e serviços](docs/05aula/entrega.md)
- [Aula 06 — Evolução do modelo](docs/06aula/entrega.md)