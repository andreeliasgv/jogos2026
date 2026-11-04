# Aula 17 — Auditoria e proteção da autenticação

## Objetivo

A aplicação passou a limitar tentativas consecutivas de autenticação e a registrar eventos estruturados de segurança. BCrypt protege senhas armazenadas, mas não impede testes on-line; por isso, o login agora possui contador persistente, janela de observação e bloqueio temporário.

## Política adotada

| Parâmetro | Valor didático | Função |
|---|---:|---|
| falhas máximas | 5 | quinta falha ativa o bloqueio |
| janela de falhas | 15 minutos | agrupa tentativas consecutivas |
| duração do bloqueio | 15 minutos | impede nova comparação BCrypt |

Os valores são externos e tipados por `ProtecaoAutenticacaoProperties`. Configurações nulas, negativas ou iguais a zero impedem a inicialização.

## Identificador e concorrência

O contador usa SHA-256 do login normalizado em minúsculas. O texto original não é persistido no controle nem nos eventos de falha. Um `upsert` PostgreSQL cria o registro inicial e `PESSIMISTIC_WRITE` serializa atualizações concorrentes, evitando perda de contagem.

No instante exato de `bloqueadaAte`, a tentativa volta a ser permitida. Um login bem-sucedido zera contador, janela e bloqueio.

## Auditoria estruturada

Os eventos registram tipo, instante, ator, alvo, hash interno de correlação e endereço de origem quando aplicável. Senhas, hashes BCrypt, JWTs, refresh tokens, hashes de refresh token, cabeçalho `Authorization` e corpos completos nunca entram na trilha.

Foram auditados:

- sucesso, falha, recusa e ativação de bloqueio no login;
- criação de conta, alteração de senha, perfis e status;
- sucesso e falha de refresh;
- logout local e global.

Falhas de login são confirmadas numa transação própria antes do `401`. Mudanças administrativas e seus eventos compartilham a mesma transação, evitando uma trilha que afirme uma alteração revertida.

## Consulta administrativa

`GET /api/auditoria-seguranca` aceita paginação e filtro opcional `tipo`. A cadeia HTTP e o service exigem `ADMINISTRADOR`. O DTO deliberadamente omite o hash de correlação.

## Matriz de ameaças

| Ameaça | Controle implementado | Evidência | Risco residual |
|---|---|---|---|
| brute force contra um login | cinco falhas, janela e bloqueio persistente | teste HTTP confirma contador e recusa | ataque pode causar bloqueio da vítima |
| falhas concorrentes | upsert e lock pessimista | repository executado com PostgreSQL real | múltiplas origens ainda alcançam o mesmo contador |
| enumeração de contas | mesma mensagem para login inexistente, senha incorreta e bloqueio | teste compara `401` genérico | tempo de resposta ainda pode formar canal lateral |
| vazamento pela auditoria | lista fechada de campos, hash do login e DTO reduzido | testes rejeitam CR/LF e hash inválido | administradores do banco ainda controlam a trilha |
| acesso indevido à trilha | `ADMINISTRADOR` no filtro e no service | operador recebe `403`; administrador recebe `200` | credencial administrativa comprometida continua poderosa |
| password spraying distribuído | eventos permitem correlação e investigação | consulta filtrada por tipo | controle por login não limita volume global ou por IP |

## Testes

`ProtecaoAutenticacaoTest` usa instantes fixos para verificar a quinta falha, fronteira exata do bloqueio, limpeza no sucesso, reinício da janela e validação de dados auditados. `ProtecaoAutenticacaoApiTest` percorre HTTP, Spring Security e PostgreSQL para comprovar resposta genérica, persistência, autorização e não exposição do hash interno.

O `clean verify` final aprovou 109 testes, sem falhas ou erros. O Liquibase reconheceu 41 changeSets. O JaCoCo mediu 78,22% das linhas e 54,49% dos branches, acima dos portões de 70% e 40%.

No teste manual, o Collection Runner executou as 12 requisições e aprovou os 16 testes sem falhas. Os retornos `401` confirmaram a resposta genérica, a ativação do bloqueio e a recusa de novas tentativas; as consultas administrativas confirmaram os eventos correspondentes sem expor os campos internos.
