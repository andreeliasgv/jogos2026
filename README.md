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

A aplicação utiliza Spring Data JPA, PostgreSQL e Liquibase. Os ambientes de desenvolvimento e testes usam bancos separados, e o Hibernate apenas valida o esquema criado pelas migrações.

Na Aula 06, o modelo foi evoluído com a entidade `Desenvolvedora` e o campo `estoqueMinimo`. A migration foi gerada de forma assistida por Liquibase Diff, revisada manualmente e aplicada preservando os dados existentes.

## Camada de aplicação

Repositories Spring Data JPA realizam o acesso aos dados. Serviços transacionais coordenam os casos de uso e preservam os comportamentos definidos no domínio.

## API REST

A partir da Aula 07, a aplicação disponibiliza endpoints REST para gêneros, desenvolvedoras e jogos.

```text
POST /api/generos
GET  /api/generos
GET  /api/generos/{id}

POST /api/desenvolvedoras
GET  /api/desenvolvedoras
GET  /api/desenvolvedoras/{id}

POST /api/jogos
GET  /api/jogos
GET  /api/jogos/{id}
```

DTOs definem os contratos de entrada e saída, mapeadores isolam as conversões e o tratamento global de exceções padroniza os erros `400`, `404` e `409`.

A API pode ser testada manualmente pela coleção Postman disponível na documentação da Aula 07.

## Entregas

- [Aula 03 — Modelo de domínio](docs/03aula/entrega.md)
- [Aula 04 — Persistência](docs/04aula/entrega.md)
- [Aula 05 — Repositories e serviços](docs/05aula/entrega.md)
- [Aula 06 — Evolução do modelo](docs/06aula/entrega.md)
- [Aula 07 — API REST, DTOs e mapeadores](docs/07aula/entrega.md)