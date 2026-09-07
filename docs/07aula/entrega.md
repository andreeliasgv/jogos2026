# Aula 07 — API REST, DTOs e mapeadores

## Objetivo

Nesta aula, a aplicação Jogos 2026 passou a disponibilizar uma API REST para cadastro e consulta de gêneros, desenvolvedoras e jogos.

A camada HTTP foi separada das entidades JPA por meio de DTOs e mapeadores manuais.

## Organização da aplicação

```text
api/controller  -> recebe requisições e define respostas HTTP
api/dto         -> representa os dados de entrada e saída da API
api/mapper      -> converte DTOs em entidades e entidades em DTOs
application     -> executa os casos de uso
repository      -> realiza o acesso ao banco de dados
domain          -> contém as entidades e regras de negócio
```

As entidades JPA não são expostas diretamente pela API. Essa separação evita acoplamento entre o contrato HTTP e o modelo de persistência.

## Recursos e endpoints

### Gêneros

| Método | Endpoint | Operação |
|---|---|---|
| `POST` | `/api/generos` | Cadastrar gênero |
| `GET` | `/api/generos/{id}` | Consultar gênero |
| `GET` | `/api/generos` | Listar gêneros |

### Desenvolvedoras

| Método | Endpoint | Operação |
|---|---|---|
| `POST` | `/api/desenvolvedoras` | Cadastrar desenvolvedora |
| `GET` | `/api/desenvolvedoras/{id}` | Consultar desenvolvedora |
| `GET` | `/api/desenvolvedoras` | Listar desenvolvedoras |

### Jogos

| Método | Endpoint | Operação |
|---|---|---|
| `POST` | `/api/jogos` | Cadastrar jogo |
| `GET` | `/api/jogos/{id}` | Consultar jogo |
| `GET` | `/api/jogos` | Listar jogos |

## DTOs e validações

Foram criados DTOs específicos de entrada e saída para os três recursos.

Os DTOs de entrada validam campos obrigatórios, tamanhos máximos, valores positivos e valores maiores ou iguais a zero. O cliente informa apenas os identificadores dos relacionamentos.

Os DTOs de saída apresentam os dados necessários ao cliente, incluindo os identificadores e nomes do gênero e da desenvolvedora, sem expor as associações JPA completas.

## Mapeadores

Os mapeadores realizam as conversões entre DTOs e entidades. Eles não consultam o banco, não iniciam transações e não definem códigos HTTP.

A resolução dos relacionamentos por identificador continua sendo responsabilidade dos serviços de aplicação.

## Códigos HTTP

| Código | Justificativa |
|---:|---|
| `200 OK` | A consulta ou listagem foi executada com sucesso. |
| `201 Created` | Um novo recurso foi persistido. A resposta também contém o cabeçalho `Location`. |
| `400 Bad Request` | O JSON é inválido ou algum campo não atende às validações de entrada. |
| `404 Not Found` | O recurso solicitado ou um relacionamento informado não existe. |
| `409 Conflict` | O cadastro entra em conflito com um código ou valor que deve ser único. |

Erros conhecidos são convertidos para o contrato padronizado `ApiError`, contendo data, status, tipo do erro, mensagem, caminho do recurso e campos inválidos.

## Testes automatizados

A camada web foi testada com Spring Boot, MockMvc e o perfil de testes.

Os testes verificam:

- cadastro de gênero com `201`;
- cadastro de desenvolvedora com `201`;
- cadastro de jogo com relacionamentos e cabeçalho `Location`;
- entrada inválida com `400`;
- recurso inexistente com `404`;
- código duplicado com `409`;
- JSON malformado com `400`;
- listagem de jogos com `200`.

A suíte completa possui 36 testes automatizados.

## Testes com Postman

Foi criada uma coleção com o caminho feliz e os cenários de erro esperados. A coleção utiliza variáveis para armazenar os identificadores produzidos durante a execução.

A coleção pode ser importada pelo arquivo:

[Jogos 2026 — Aula 07](Jogos-2026-Aula-07.postman_collection.json)

A coleção não contém senhas, tokens ou credenciais do banco de dados.

## Resultado

A aplicação agora possui uma API REST com contratos explícitos, validação de entrada, mapeamento entre camadas, códigos HTTP adequados, erros padronizados e testes manuais e automatizados.