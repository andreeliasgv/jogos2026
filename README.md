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
Pessoa 1 ───── 0..1 ContaAcesso 1 ───── 1..N PerfilAcesso
Cliente 1 ──── 0..N Venda
Colaborador 1  0..N Venda
Venda 1 ────── 1..N ItemVenda N ───── 1 Jogo
```

## Persistência

A aplicação utiliza Spring Data JPA, PostgreSQL e Liquibase. Os ambientes de desenvolvimento e testes usam bancos separados, e o Hibernate apenas valida o esquema criado pelas migrações.

Na Aula 06, o modelo foi evoluído com a entidade `Desenvolvedora` e o campo `estoqueMinimo`. A migration foi gerada de forma assistida por Liquibase Diff, revisada manualmente e aplicada preservando os dados existentes.

Na Aula 08, um índice funcional passou a garantir a unicidade do nome normalizado dos gêneros. As pesquisas paginadas utilizam `Specification` e os relacionamentos necessários à resposta de jogos são carregados com `EntityGraph`.

Na Aula 10, a migration `005-dominio-comercial-vendas.yaml` acrescentou fornecedores, pessoas, clientes, colaboradores, vendas e itens. O item preserva quantidade e preço praticado, enquanto as chaves e constraints mantêm a integridade entre os novos recursos.

Na Aula 12, a migration `006-contas-acesso.yaml` acrescentou contas e perfis. O banco garante uma conta por pessoa, login único normalizado e valores conhecidos de status e perfil.

Na Aula 16, a migration `007-sessoes-refresh-token.yaml` acrescentou sessões renováveis. Somente o hash SHA-256 do refresh token é armazenado, com famílias, rotação de uso único e motivos de revogação. O histórico possui agora 38 changesets.

Na Aula 17, a migration `008-auditoria-protecao-autenticacao.yaml` acrescentou o controle persistente de tentativas e eventos estruturados de segurança. O histórico possui agora 41 changeSets.

## Camada de aplicação

Repositories Spring Data JPA realizam o acesso aos dados. Serviços transacionais coordenam os casos de uso e preservam os comportamentos definidos no domínio.

As entidades concentram as regras de alteração, status, associações e estoque. Os serviços resolvem identificadores, delimitam transações e coordenam as operações sem transferir regras de negócio para os controllers.

Pessoa representa a identidade compartilhada. Cliente e Colaborador são papéis opcionais modelados por composição, permitindo que a mesma pessoa exerça ambos sem herança JPA. A finalização de uma venda valida todos os itens e baixa seus estoques em uma única transação.

Na Aula 11, finalizações de vendas e movimentações manuais passaram a adquirir locks pessimistas nos jogos antes de alterar o estoque. Vendas com vários itens bloqueiam os jogos por ID crescente, reduzindo o risco de deadlock. O teste concorrente usa duas transações reais e comprova que somente uma venda consome um saldo disputado.

Na Aula 12, `ContaAcesso` passou a concentrar login, hash BCrypt, status e perfis sem misturar credenciais com `Pessoa`, `Cliente` ou `Colaborador`. O Spring Security carrega as contas do PostgreSQL e protege todas as rotas que não foram explicitamente declaradas públicas.

Na Aula 13, login e senha passaram a ser enviados somente ao endpoint de autenticação. A API emite um JWT assinado com HS256 e validade absoluta de 30 minutos; o OAuth2 Resource Server valida a assinatura, o emissor e os claims temporais em cada chamada protegida.

Na Aula 14, os perfis do JWT passaram a ser convertidos em authorities. Operadores executam atividades cotidianas, enquanto criação e gestão de contas e exclusões exigem `ADMINISTRADOR`. A política é aplicada tanto na cadeia HTTP quanto nos serviços administrativos.

Na Aula 15, a suíte foi organizada como uma estratégia em camadas. Testes unitários de domínio e aplicação fornecem retorno rápido; testes com Spring, PostgreSQL, `MockMvc`, segurança e concorrência validam as fronteiras reais do sistema.

Na Aula 16, o login passou a emitir um par de tokens. O access token continua stateless e curto; o refresh token é opaco, persistido somente como hash e rotacionado a cada uso. Inatividade, validade absoluta, reutilização, logout local e logout global encerram novas renovações conforme a regra correspondente.

Na Aula 17, cinco falhas dentro de quinze minutos passaram a bloquear temporariamente novas tentativas. Login e operações sensíveis geram eventos sem senhas ou tokens, consultáveis apenas por administradores.

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

POST /api/contas-acesso
GET  /api/contas-acesso/me
PUT  /api/contas-acesso/me/senha
PUT  /api/contas-acesso/{id}/perfis
PUT  /api/contas-acesso/{id}/status

POST /api/autenticacao/login
POST /api/autenticacao/refresh
POST /api/autenticacao/logout
POST /api/autenticacao/logout-todos
GET  /api/auditoria-seguranca
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

Na Aula 12, a API passou a armazenar senhas somente como hash `{bcrypt}` e a carregar contas do banco com Spring Security. Na Aula 13, o HTTP Basic foi substituído por JWT: health, documentação, login e o bootstrap provisório de pessoa e conta permanecem públicos; as demais rotas exigem `Authorization: Bearer <token>`.

O token contém emissor, login, instante de emissão, expiração, identificador e perfis controlados pelo servidor. Seu payload é codificado, não criptografado, e por isso não contém senha nem informações confidenciais. A chave real permanece somente no `.env` local.

A coleção Postman da Aula 13 automatiza o bootstrap, captura o JWT após o login e valida o acesso Bearer e os principais cenários de rejeição em 20 testes.

Na Aula 14, o bootstrap público foi encerrado. A primeira conta administrativa é provisionada uma única vez por configuração externa e recebe `OPERADOR` e `ADMINISTRADOR`. Depois disso, a flag de provisionamento deve ser desligada e novas contas passam a exigir um token administrativo.

A suíte automatizada possui 109 testes entre domínio, persistência, aplicação, segurança, API e contrato OpenAPI. O JaCoCo integra o ciclo `verify`, gera o relatório em `target/site/jacoco` e exige no mínimo 70% de linhas e 40% de branches cobertas.

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
- [Aula 12 — Autenticação e proteção de senhas](docs/12aula/entrega.md)
- [Aula 13 — JWT, Bearer Token e OAuth2 Resource Server](docs/13aula/entrega.md)
- [Aula 14 — Autorização, perfis e controle de acesso](docs/14aula/entrega.md)
- [Aula 15 — Estratégia de testes e cobertura com JaCoCo](docs/15aula/entrega.md)
- [Aula 16 — Ciclo de vida dos tokens](docs/16aula/entrega.md)
- [Aula 17 — Auditoria e proteção da autenticação](docs/17aula/entrega.md)
