# Aula 13 — JWT, Bearer Token e OAuth2 Resource Server

## Objetivo

Substituir o envio de login e senha em todas as requisições por um fluxo de autenticação em duas etapas: as credenciais são apresentadas uma vez ao endpoint de login e, quando válidas, produzem um JWT temporário. As rotas protegidas passam a aceitar esse token no esquema Bearer.

## Fluxo implementado

```text
POST /api/autenticacao/login (login + senha)
        ↓
AuthenticationManager valida a conta e o BCrypt
        ↓
TokenService emite JWT assinado com HS256
        ↓
Cliente recebe accessToken com validade de 30 minutos
        ↓
Authorization: Bearer <accessToken>
        ↓
OAuth2 Resource Server valida assinatura, emissor e tempo
```

O `AuthenticationManager` reaproveita o processo da Aula 12. Assim, não há uma segunda implementação de comparação de senha: o Spring Security continua consultando o `UserDetailsService`, comparando o hash BCrypt e verificando o estado da conta.

## Estrutura e claims do JWT

Um JWT assinado possui três segmentos:

```text
header.payload.signature
```

- o **header** informa o tipo e o algoritmo, fixado em HS256;
- o **payload** contém as afirmações sobre a autenticação;
- a **assinatura** permite detectar alterações e comprovar que o emissor conhecia a chave.

O token emitido contém:

| Claim | Finalidade |
|---|---|
| `iss` | identifica a API emissora |
| `sub` | identifica o login autenticado |
| `iat` | registra o instante de emissão |
| `exp` | limita a validade a 30 minutos |
| `jti` | identifica unicamente o token |
| `perfis` | registra os perfis vindos da conta autenticada |

Os perfis são obtidos do objeto `Authentication` produzido pelo servidor. Eles nunca são aceitos do JSON de login, pois isso permitiria ao cliente declarar os próprios privilégios.

## Assinatura não é criptografia

O payload usa Base64URL e pode ser decodificado por qualquer pessoa que receba o token. A assinatura garante integridade e autenticidade, mas não confidencialidade. Por isso, senha, CPF, segredos e outros dados sensíveis não devem aparecer nos claims.

Foi usado **HS256**, em que a mesma chave assina e valida. Essa escolha é adequada ao laboratório porque uma única aplicação executa as duas tarefas. Em uma arquitetura distribuída, **RS256** ou um provedor de identidade permitiria que serviços validassem com uma chave pública sem receber poder para emitir tokens.

## Gestão da chave

A chave aleatória de 256 bits foi gerada localmente e armazenada somente em `.env`, arquivo ignorado pelo Git. O `.env.example` possui apenas um marcador de substituição. O profile de testes usa uma chave conhecida e exclusiva para automação.

Na inicialização, `JwtConfig`:

1. decodifica a configuração Base64;
2. exige ao menos 32 bytes;
3. cria a chave HMAC-SHA256;
4. configura encoder e decoder exclusivamente para HS256;
5. valida o emissor esperado, a expiração e os claims temporais.

## Resource Server

A aplicação passou a usar o suporte oficial do Spring Security para OAuth2 Resource Server. O filtro procura o Bearer Token, chama o `JwtDecoder` e cria a autenticação da requisição. Não foi criado um filtro JWT manual.

Apesar do nome do componente, esta aplicação não é um Authorization Server completo. Ela possui um endpoint didático que emite tokens para suas próprias contas, mas não implementa clients, grants, consentimento, metadados ou os demais protocolos de um servidor de autorização.

Continuam públicas apenas as rotas necessárias ao laboratório:

- health e documentação OpenAPI;
- criação provisória de pessoa e conta para bootstrap;
- `POST /api/autenticacao/login`.

As demais rotas exigem `Authorization: Bearer <token>`. HTTP Basic deixou de liberar acesso.

## Expiração, bloqueio e logout

O access token vence em um horário absoluto, 30 minutos depois da emissão. Usá-lo não renova esse prazo. Como a validação é stateless, bloquear uma conta impede novos logins, mas não revoga automaticamente um token já emitido; ele pode continuar válido até `exp`.

Também não há logout no servidor nesta aula. Apagar o token no cliente não invalida uma cópia que tenha sido roubada. Sistemas reais podem combinar tokens curtos com refresh token, lista de revogação por `jti`, introspecção ou um Authorization Server.

## OpenAPI, Postman e testes

O OpenAPI anuncia `bearerAuth` com formato JWT. A coleção `Jogos-2026-Aula-13.postman_collection.json` cria uma pessoa e uma conta fictícias, efetua o login, salva `accessToken` como variável e o reutiliza nas chamadas protegidas. Ela também comprova a rejeição de requisição sem token, token malformado, HTTP Basic e senha incorreta. A execução manual completa terminou com **9 requisições, 20 testes aprovados e nenhuma falha**.

O script do Postman decodifica o payload apenas para fins didáticos. Essa inspeção não valida a assinatura; somente o `JwtDecoder` do servidor decide se o token é confiável.

Os testes automatizados verificam emissão, duração, emissor, sujeito, `jti`, perfis, autenticação Bearer e rejeição de credenciais ou tokens inválidos. A suíte completa terminou com **77 testes, nenhuma falha e nenhum erro**. O valor real da chave e tokens ativos não fazem parte desta entrega.

## Arquivos principais

- `JwtProperties` e `JwtConfig`: configuração tipada e infraestrutura criptográfica;
- `AutenticacaoService` e `TokenService`: autenticação e emissão;
- `AutenticacaoController`: endpoint público de login;
- `SecurityConfig`: proteção das rotas como Resource Server;
- `OpenApiConfig`: documentação do esquema Bearer;
- `ContaAcessoSecurityTest`: cenários automatizados de segurança;
- coleção Postman da Aula 13: validação externa do fluxo.
