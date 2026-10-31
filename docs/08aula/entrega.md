# Aula 08 — CRUD completo, estoque e consultas paginadas

## Objetivo

Nesta aula, a API Jogos 2026 evoluiu das operações básicas de cadastro e consulta para um CRUD mais completo de gêneros e jogos.

Foram adicionadas alteração, ativação e inativação, exclusão, movimentação de estoque, filtros combináveis, ordenação e paginação. As regras permaneceram distribuídas entre domínio, serviços, repositories e controllers, sem expor entidades JPA diretamente pela API.

## Contrato HTTP

### Gêneros

| Método | Endpoint | Operação |
|---|---|---|
| `POST` | `/api/generos` | Cadastrar gênero |
| `GET` | `/api/generos/{id}` | Consultar gênero por ID |
| `GET` | `/api/generos` | Pesquisar gêneros com filtros e paginação |
| `PUT` | `/api/generos/{id}` | Alterar os dados editáveis |
| `PUT` | `/api/generos/{id}/status` | Ativar ou inativar |
| `DELETE` | `/api/generos/{id}` | Excluir um gênero sem jogos associados |

### Jogos

| Método | Endpoint | Operação |
|---|---|---|
| `POST` | `/api/jogos` | Cadastrar jogo |
| `GET` | `/api/jogos/{id}` | Consultar jogo por ID |
| `GET` | `/api/jogos` | Pesquisar jogos com filtros e paginação |
| `PUT` | `/api/jogos/{id}` | Alterar os dados editáveis |
| `PUT` | `/api/jogos/{id}/status` | Ativar ou inativar |
| `POST` | `/api/jogos/{id}/estoque/entradas` | Registrar entrada de unidades |
| `POST` | `/api/jogos/{id}/estoque/saidas` | Registrar saída de unidades |
| `DELETE` | `/api/jogos/{id}` | Excluir jogo |

## Semântica das operações

O `PUT` foi utilizado para substituir os dados editáveis de um recurso. Repetir a mesma requisição deixa o cadastro no mesmo estado final, caracterizando uma operação idempotente.

As movimentações de estoque utilizam `POST` porque representam eventos. Repetir uma entrada de cinco unidades acrescenta outras cinco unidades e, portanto, produz um novo efeito.

O `DELETE` representa a remoção física. Inativar é uma operação diferente: o registro e seu identificador permanecem no banco e o status pode ser alterado novamente no futuro.

## Campos controlados pela aplicação

O DTO de alteração de jogo permite modificar:

- título;
- preço unitário;
- estoque mínimo;
- gênero;
- desenvolvedora opcional.

O código do jogo, a quantidade disponível, a data de cadastro e o status não pertencem ao DTO de alteração geral. A quantidade muda somente pelas rotas de estoque, enquanto o status possui uma rota específica.

## Regras de domínio

As entidades continuam responsáveis pelas invariantes do negócio.

Ao trocar o gênero de um jogo, os dois lados da associação em memória são atualizados. O jogo é removido da coleção do gênero anterior e incluído na coleção do novo gênero.

A entrada e a saída de unidades reutilizam comportamentos da entidade `Jogo`. Quantidades não positivas e retiradas superiores à quantidade disponível são rejeitadas, impedindo estoque negativo.

## Integridade e persistência

A migration `004-unique-nome-genero-jogo.yaml` criou um índice funcional sobre `LOWER(BTRIM(nome))`. Dessa forma, nomes que diferem apenas por maiúsculas, minúsculas ou espaços externos são considerados duplicados pelo PostgreSQL.

A verificação de duplicidade também é feita pelo serviço para produzir uma mensagem compreensível. O banco permanece como a proteção definitiva para concorrência e acessos externos.

Um gênero que possui jogos não pode ser excluído. O serviço verifica o relacionamento antes da exclusão e lança `RecursoEmUsoException`. O `flush()` antecipa uma eventual violação de integridade para dentro do caso de uso.

As alterações em entidades carregadas dentro de uma transação utilizam o dirty checking do JPA. Não é necessário chamar `save` depois de cada mudança em uma entidade gerenciada.

## Filtros, paginação e ordenação

Os repositories de gêneros e jogos passaram a implementar `JpaSpecificationExecutor`. As `Specification`s acrescentam condições somente quando o parâmetro correspondente é informado, permitindo combinar filtros no banco de dados.

### Filtros de gêneros

- trecho do nome, sem diferenciar maiúsculas de minúsculas;
- status.

### Filtros de jogos

- trecho do título, sem diferenciar maiúsculas de minúsculas;
- status;
- identificador do gênero;
- identificador da desenvolvedora;
- quantidade abaixo do estoque mínimo.

A resposta paginada utiliza `PaginaResponse`, com conteúdo, número e tamanho da página, total de elementos, total de páginas e indicadores de primeira e última página.

O tamanho padrão é 20 e o tamanho máximo permitido é 100. Os serviços também mantêm uma lista explícita de campos permitidos para ordenação.

O `EntityGraph` da pesquisa de jogos carrega gênero e desenvolvedora dentro da transação. Isso permite que o mapper produza o DTO de resposta com `spring.jpa.open-in-view=false`.

## Códigos HTTP e erros

| Código | Situação |
|---:|---|
| `200 OK` | Consulta, pesquisa, alteração, status ou movimentação concluída |
| `201 Created` | Recurso cadastrado, com cabeçalho `Location` |
| `204 No Content` | Recurso excluído sem corpo de resposta |
| `400 Bad Request` | DTO, JSON, quantidade, saldo ou ordenação inválida |
| `404 Not Found` | Recurso ou relacionamento inexistente |
| `409 Conflict` | Valor duplicado ou tentativa de excluir gênero em uso |

Os erros conhecidos continuam sendo convertidos para `ApiError`. Violações de regras de domínio retornam `400`, recursos ausentes retornam `404` e conflitos com o estado atual dos dados retornam `409`.

## Testes automatizados

A suíte completa possui 52 testes automatizados, todos concluídos sem falhas ou erros.

| Camada | Classe | Quantidade |
|---|---|---:|
| API | `GeneroJogoApiTest` | 5 |
| API | `JogoApiTest` | 14 |
| Aplicação | `JogoServiceTest` | 4 |
| Domínio | `DesenvolvedoraTest` | 4 |
| Domínio | `GeneroJogoTest` | 7 |
| Domínio | `JogoTest` | 11 |
| Persistência | `PersistenciaJpaTest` | 6 |
| Contexto da aplicação | `Jogos2026ApplicationTests` | 1 |

Os testes cobrem caminhos de sucesso e de erro, incluindo alteração, status, associações, movimentação de estoque, paginação, filtros, ordenação inválida, exclusão e conflito de integridade.

## Testes com Postman

A coleção da Aula 08 possui 24 requisições organizadas na ordem do fluxo. Ela cria dados com valores únicos, armazena os identificadores em variáveis, testa as operações e finaliza removendo o jogo e o gênero criados.

A coleção também verifica os erros esperados `400`, `404` e `409`. Ela não contém senhas, tokens ou credenciais do banco de dados.

[Jogos 2026 — Coleção Postman da Aula 08](Jogos-2026-Aula-08.postman_collection.json)

## Resultado

A aplicação agora possui manutenção completa de gêneros e jogos, movimentação segura de estoque e consultas escaláveis executadas no banco. O contrato HTTP diferencia alteração, transição de status, movimentação e exclusão, mantendo as regras de negócio fora dos controllers.
