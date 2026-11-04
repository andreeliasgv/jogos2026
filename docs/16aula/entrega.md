# Aula 16 — Ciclo de vida dos tokens

## Objetivo

A autenticação passou a combinar um access token JWT de curta duração com um refresh token opaco. O JWT continua sendo validado sem consulta de sessão, enquanto o refresh token introduz estado controlado no servidor para permitir renovação, rotação e revogação.

## Modelo da sessão

Cada login cria uma família identificada pelo claim `sid` do JWT. O refresh token possui 32 bytes aleatórios gerados por `SecureRandom` e é codificado em Base64 URL-safe. Apenas seu hash SHA-256 é persistido; portanto, uma leitura indevida do banco não revela a credencial utilizável.

A tabela `sessao_refresh_token` registra conta, família, hash, criação, última atividade, validade absoluta, utilização e revogação. A migration 007 adiciona chave estrangeira, unicidade do hash, índices por família e conta e constraints de expiração e motivo.

## Três limites temporais

- o access token expira em 30 minutos e não é renovado em si;
- a sessão deixa de renovar após 30 minutos de inatividade;
- toda a família termina, no máximo, 8 horas depois do login.

Os valores usam `Duration` ISO-8601 e podem ser configurados por ambiente. A regra considera o instante exatamente igual ao limite como expirado.

## Rotação e reutilização

Uma renovação válida marca o refresh token como utilizado e cria um sucessor na mesma família, preservando o limite absoluto. Uma consulta com `PESSIMISTIC_WRITE` serializa usos concorrentes do mesmo token.

Se um token utilizado aparecer novamente, a família inteira é revogada com motivo `REUTILIZACAO`. A transação usa `noRollbackFor` para que essa revogação seja confirmada mesmo quando o caso de uso termina lançando a exceção que produz HTTP 401.

## Contratos HTTP

| Método e rota | Autenticação | Resultado |
|---|---|---|
| `POST /api/autenticacao/login` | login e senha | cria família e devolve os dois tokens |
| `POST /api/autenticacao/refresh` | refresh token | rotaciona e devolve um novo par |
| `POST /api/autenticacao/logout` | refresh token | revoga a família atual e retorna 204 |
| `POST /api/autenticacao/logout-todos` | Bearer JWT | revoga todas as famílias da conta e retorna 204 |

Token desconhecido, expirado, revogado, reutilizado ou associado a conta inativa recebe a mensagem genérica `Refresh token inválido ou expirado`.

## Decisões arquiteturais

1. **Access token no cliente:** em memória, evitando persistência desnecessária de uma credencial Bearer.
2. **Refresh token no cliente:** em armazenamento protegido; numa aplicação web real, preferencialmente cookie `HttpOnly`, `Secure` e `SameSite`, embora esta API didática o transporte em JSON.
3. **Durações:** 30 minutos de inatividade e 8 horas absolutas equilibram conveniência didática e limitação da janela de abuso.
4. **Access token após logout:** permanece válido até seu claim `exp`; a implementação revoga a renovação, não mantém uma denylist de JWTs.
5. **Ameaça reduzida pela rotação:** reutilização de um refresh token copiado, detectada quando uma geração já consumida reaparece.
6. **Renovações simultâneas:** o lock pessimista permite que uma consuma o token; a segunda observa a reutilização e revoga a família.

## Testes e evidências

`SessaoRefreshTokenTest` verifica fronteiras temporais, uso único e preservação do primeiro motivo de revogação. `CicloTokenApiTest` atravessa Spring Security, PostgreSQL e HTTP para comprovar rotação, detecção de reutilização, logout local, logout global e a validade residual do JWT.

A coleção `Jogos-2026-Aula-16.postman_collection.json` reproduz o ciclo manual sem armazenar senha no arquivo. A senha administrativa deve ser preenchida somente como valor local no Postman.

O `clean verify` final aprovou 99 testes, sem falhas ou erros. O JaCoCo mediu 76,41% das linhas e 51,76% dos branches, mantendo o projeto acima dos portões de 70% e 40%. O Liquibase confirmou 38 changeSets.

No teste manual, o Collection Runner executou as 12 requisições e aprovou os 13 testes sem falhas. Os retornos `401` dos cenários de reutilização e revogação foram confirmados como comportamento esperado.
