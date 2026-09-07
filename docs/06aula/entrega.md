# Aula 06 — Evolução do modelo com Liquibase Diff

## Evolução do domínio

Foi adicionada a entidade `Desenvolvedora`, identificada por um código
único. Cada jogo pode possuir uma desenvolvedora principal, mas a
associação é opcional para preservar registros anteriores.

Também foi adicionado `estoqueMinimo` em `Jogo`. O tipo `INTEGER` foi
escolhido porque os jogos são controlados em unidades indivisíveis.

## Geração assistida

Foram utilizados dois bancos descartáveis:

- `jogos2026_diff`, contendo o esquema anterior com as migrations 001 e 002;
- `jogos2026_reference`, gerado pelo Hibernate a partir das entidades atuais.

O Liquibase comparou os dois bancos e criou um rascunho em
`target/liquibase-diff/changelog-gerado.yaml`.

## Problemas encontrados no rascunho

| Resultado automático | Decisão adotada |
|---|---|
| IDs baseados em timestamp | Utilizar IDs estáveis de `003-01` a `003-09` |
| Autor gerado pela máquina | Utilizar o autor `andre` |
| PK chamada `desenvolvedora_pkey` | Utilizar `pk_desenvolvedora` |
| Remoção e recriação da FK de gênero | Descartar por ser ruído perigoso |
| `estoque_minimo` criado diretamente como `NOT NULL` | Aplicar expandir, preencher e restringir |
| Ausência de checks | Criar checks de status e estoque mínimo |
| FKs geradas com `NO ACTION` | Declarar `RESTRICT` conscientemente |
| Ausência de rollback e comentários | Documentar intenção e operação inversa |

## Estratégia para estoque mínimo

A coluna foi adicionada em três etapas:

1. criação inicialmente permitindo `NULL`;
2. preenchimento dos jogos antigos com zero;
3. aplicação de `NOT NULL`.

Depois foi adicionada a constraint que impede estoque mínimo negativo.

## Migration final

A migration `003-desenvolvedora-e-estoque-minimo.yaml` contém nove
changeSets. Com as migrations anteriores, o banco passou a registrar
17 changeSets.

## Preservação de dados

Antes da migration foi inserido o jogo `MIGRACAO-001` no esquema antigo.
Após aplicar a migration:

- o jogo continuou existente;
- `estoque_minimo` recebeu zero;
- `desenvolvedora_id` permaneceu nulo.

## Convergência

Após recriar o banco de referência, um novo diff não encontrou diferenças
de tabelas ou colunas. Restaram apenas sugestões para substituir
`RESTRICT` por `NO ACTION` nas chaves estrangeiras, diferença conhecida
e conscientemente rejeitada.

## Evidências

- 17 changeSets aplicados;
- dados anteriores preservados;
- Hibernate validando o esquema;
- código duplicado de desenvolvedora rejeitado pelo banco;
- desenvolvedora opcional;
- estoque mínimo não negativo;
- 28 testes executados sem falhas;
- `.env` e bancos descartáveis não versionados.