# Jogos 2026

API desenvolvida durante a disciplina de Programação.

## Tema

API para controle de jogos de uma loja, organizados por gênero.

## Modelo de domínio

```text
GeneroJogo     1 ─────── 0..N Jogo
Desenvolvedora 0..1 ──── 0..N Jogo
Fornecedor     0..1 ──── 0..N Jogo

Pessoa 1 ───── 0..1 Cliente
Pessoa 1 ───── 0..1 Colaborador
Cliente 1 ──── 0..N Venda
Colaborador 1  0..N Venda
Venda 1 ────── 1..N ItemVenda N ───── 1 Jogo
```

## Persistência

A aplicação utiliza Spring Data JPA, PostgreSQL e Liquibase. Os ambientes de desenvolvimento e testes usam bancos separados, e o Hibernate apenas valida o esquema criado pelas migrações.

Na Aula 06, o modelo foi evoluído com a entidade `Desenvolvedora` e o campo `estoqueMinimo`. A migration foi gerada de forma assistida por Liquibase Diff, revisada manualmente e aplicada preservando os dados existentes.

Na Aula 08, um índice funcional passou a garantir a unicidade do nome normalizado dos gêneros. As pesquisas paginadas utilizam `Specification` e os relacionamentos necessários à resposta de jogos são carregados com `EntityGraph`.

Na Aula 10, a migration `005-dominio-comercial-vendas.yaml` acrescentou fornecedores, pessoas, clientes, colaboradores, vendas e itens. O item preserva quantidade e preço praticado, enquanto as chaves e constraints mantêm a integridade entre os novos recursos.

## Camada de aplicação

Repositories Spring Data JPA realizam o acesso aos dados. Serviços transacionais coordenam os casos de uso e preservam os comportamentos definidos no domínio.

As entidades concentram as regras de alteração, status, associações e estoque. Os serviços resolvem identificadores, delimitam transações e coordenam as operações sem transferir regras de negócio para os controllers.

Pessoa representa a identidade compartilhada. Cliente e Colaborador são papéis opcionais modelados por composição, permitindo que a mesma pessoa exerça ambos sem herança JPA. A finalização de uma venda valida todos os itens e baixa seus estoques em uma única transação.

Na Aula 11, finalizações de vendas e movimentações manuais passaram a adquirir locks pessimistas nos jogos antes de alterar o estoque. Vendas com vários itens bloqueiam os jogos por ID crescente, reduzindo o risco de deadlock. O teste concorrente usa duas transações reais e comprova que somente uma venda consome um saldo disputado.

## API REST

A partir da Aula 07, a aplicação disponibiliza endpoints REST para gêneros, desenvolvedoras e jogos. Na Aula 08, gêneros e jogos receberam alteração, status, exclusão e consultas paginadas; jogos também receberam movimentações de estoque.

```text
POST /api/generos
GET  /api/generos
GET  /api/generos/{id}
PUT  /api/generos/{id}
PUT  /api/generos/{id}/status
DELETE /api/generos/{id}

POST /api/desenvolvedoras
GET  /api/desenvolvedoras
GET  /api/desenvolvedoras/{id}

POST /api/jogos
GET  /api/jogos
GET  /api/jogos/{id}
PUT  /api/jogos/{id}
PUT  /api/jogos/{id}/status
POST /api/jogos/{id}/estoque/entradas
POST /api/jogos/{id}/estoque/saidas
DELETE /api/jogos/{id}

POST /api/fornecedores
GET  /api/fornecedores
GET  /api/fornecedores/{id}

POST /api/pessoas
GET  /api/pessoas
GET  /api/pessoas/{id}
PUT  /api/pessoas/{id}
PUT  /api/pessoas/{id}/status

POST /api/clientes
GET  /api/clientes
GET  /api/clientes/{id}
PUT  /api/clientes/{id}

POST /api/colaboradores
GET  /api/colaboradores
GET  /api/colaboradores/{id}
PUT  /api/colaboradores/{id}

POST   /api/vendas
GET    /api/vendas
GET    /api/vendas/{id}
POST   /api/vendas/{id}/itens
DELETE /api/vendas/{vendaId}/itens/{itemId}
POST   /api/vendas/{id}/finalizacao
POST   /api/vendas/{id}/cancelamento
```

DTOs definem os contratos de entrada e saída, mapeadores isolam as conversões e o tratamento global de exceções padroniza os erros `400`, `404` e `409`.

As pesquisas de gêneros e jogos aceitam filtros opcionais, ordenação e os parâmetros `page` e `size`. A resposta utiliza um contrato estável com o conteúdo e os metadados da página. O tamanho padrão é 20 e o limite máximo é 100.

A API pode ser testada manualmente pela coleção Postman disponível na documentação da Aula 08.

Na Aula 09, o contrato HTTP passou a ser publicado em OpenAPI e apresentado pelo Swagger UI. Controllers, operações, parâmetros, respostas, paginação, DTOs e o formato `ApiError` possuem descrições e exemplos fictícios próprios do domínio.

Com a aplicação executada no profile de desenvolvimento, a documentação fica disponível em:

```text
OpenAPI JSON: http://localhost:8080/v3/api-docs
OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
Swagger UI:   http://localhost:8080/swagger-ui.html
```

No profile de produção, o documento e a interface permanecem desabilitados até que exista uma decisão explícita de publicação.

Na Aula 10, CPF e CNPJ passaram a ser validados por dígitos verificadores tanto no domínio quanto na entrada HTTP. Vendas preservam o preço histórico dos jogos e só baixam o estoque quando finalizadas. A coleção Postman da aula executa um cenário comercial completo com 17 requisições.

A suíte automatizada possui 67 testes entre domínio, persistência, aplicação, API e contrato OpenAPI.

## Entregas

- [Aula 03 — Modelo de domínio](docs/03aula/entrega.md)
- [Aula 04 — Persistência](docs/04aula/entrega.md)
- [Aula 05 — Repositories e serviços](docs/05aula/entrega.md)
- [Aula 06 — Evolução do modelo](docs/06aula/entrega.md)
- [Aula 07 — API REST, DTOs e mapeadores](docs/07aula/entrega.md)
- [Aula 08 — CRUD completo, estoque e consultas paginadas](docs/08aula/entrega.md)
- [Aula 09 — Documentação da API com OpenAPI](docs/09aula/entrega.md)
- [Aula 10 — Pessoas, fornecedores e vendas](docs/10aula/entrega.md)
- [Aula 11 — Concorrência de estoque e locking](docs/11aula/entrega.md)
