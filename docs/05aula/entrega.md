# Aula 05 — Repositories, serviços e transações

## Responsabilidades

| Camada | Responsabilidade no projeto |
|---|---|
| Domínio | Preservar regras como quantidade válida e associação entre gênero e jogo |
| Repository | Consultar e persistir entidades por meio do Spring Data JPA |
| Serviço | Coordenar casos de uso e definir a fronteira transacional |
| PostgreSQL | Armazenar dados e aplicar constraints de integridade |

## Repositories

`GeneroJogoRepository` fornece consultas por nome sem diferenciar
maiúsculas e minúsculas.

`JogoRepository` fornece consultas por código, gênero e status. O Spring
Data cria as implementações dessas interfaces durante a inicialização.

## Serviços e transações

`GeneroJogoService` coordena cadastro, busca e listagem de gêneros.

`JogoService` verifica duplicidade, localiza o gênero, executa a
associação pelo domínio e persiste o jogo dentro da mesma transação.

As consultas usam transações `readOnly`. Os casos de uso que alteram
estado usam transações de escrita.

## Dirty checking

`JogoService.receberUnidades` altera uma entidade gerenciada sem chamar
`save` novamente. Ao concluir a transação, o Hibernate detecta a mudança
e executa o `UPDATE`.

## Exceções

`RecursoNaoEncontradoException` e `RecursoDuplicadoException` representam
falhas da aplicação sem depender de conceitos HTTP.

## Evidências

- dois repositories encontrados pelo Spring Data JPA;
- cadastro de jogo com gênero persistido;
- código duplicado rejeitado pelo serviço;
- gênero inexistente não deixa o jogo salvo;
- atualização de quantidade comprovada após `flush`, `clear` e releitura;
- dados dos testes desfeitos por rollback;
- 21 testes executados com sucesso no PostgreSQL;
- nenhum controller ou DTO antecipado.