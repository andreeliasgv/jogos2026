# Aula 15 — Estratégia de testes e cobertura com JaCoCo

## Objetivo

Nesta aula, a suíte deixou de ser tratada apenas como uma coleção de testes verdes e passou a ser analisada como um sistema de evidências. Cada nível responde a perguntas diferentes: objetos em memória protegem invariantes rapidamente; integrações com PostgreSQL validam mapeamentos e transações; `MockMvc` atravessa contrato HTTP e filtros; e o JaCoCo mostra quais estruturas foram exercitadas.

Cobertura não responde se as asserções estão corretas. Ela informa somente que determinado bytecode foi alcançado. Por isso, as métricas são interpretadas junto com risco, qualidade do oráculo e custo do teste.

## Verificação, validação e falhas

- **verificação** pergunta se a implementação corresponde à especificação, como esperar `403` quando um operador tenta administrar contas;
- **validação** pergunta se a solução atende à necessidade, como avaliar se os perfis escolhidos representam responsabilidades reais;
- um **erro humano** pode introduzir um **defeito** no código, que produz uma **falha observável** durante a execução.

Um teste é uma experiência controlada: prepara um estado, provoca uma ação e compara o observado com um oráculo. O oráculo deve vir da regra ou do contrato, não da repetição da própria implementação.

## Inventário da suíte

| Nível | Exemplo no projeto | Infraestrutura | Evidência principal | Limite |
|---|---|---|---|---|
| unitário de domínio | `JogoTest`, `ContaAcessoTest` | nenhuma | invariantes e transições em memória | não valida JPA ou HTTP |
| unitário de aplicação | `JogoServiceUnitTest` | Mockito | decisões do service e colaboração com repositories | mocks não provam integração |
| integração de aplicação | `JogoServiceTest`, `ContaAcessoServiceTest` | Spring e PostgreSQL | transações, repositories e dirty checking | feedback mais lento |
| persistência | `PersistenciaJpaTest` | JPA, Liquibase e PostgreSQL | mapeamentos e constraints reais | não valida contrato HTTP |
| componente/API | `JogoApiTest`, `GeneroJogoApiTest` | Spring e `MockMvc` | validação, serialização e status | não abre TCP real |
| contrato | `OpenApiDocumentationTest` | SpringDoc e `MockMvc` | documento OpenAPI publicado | descrição não aplica política |
| segurança | `ContaAcessoSecurityTest` | Spring Security, JWT e banco | diferenças entre `401`, `403` e sucesso | não substitui teste unitário |
| concorrência | `VendaServiceTest` | PostgreSQL, transações e threads | locking e disputa por estoque | maior custo e complexidade |

Essa distribuição segue a ideia econômica da pirâmide: muitos testes baratos e localizados, integrações seletivas nas fronteiras reais e poucos cenários amplos.

## Arrange–Act–Assert

Os novos testes foram organizados em três momentos:

1. **Arrange:** criação do jogo, gênero e comportamento dos mocks;
2. **Act:** execução de um único caso de uso do `JogoService`;
3. **Assert:** comparação com o resultado esperado e, quando a interação faz parte do contrato, confirmação do lock ou da persistência.

Nomes como `naoDeveSalvarQuandoGeneroNaoExiste` comunicam condição e resultado. Isso transforma o relatório Maven em parte do diagnóstico.

## `ContaAcessoTest`

O teste unitário de conta foi ampliado para oito execuções. O método parametrizado representa quatro amostras da mesma partição inválida: `null`, string vazia, um espaço e três espaços. Cada argumento aparece separadamente no relatório.

Também são verificados:

- normalização do login e do hash;
- perfil inicial `OPERADOR`;
- rejeição de conta sem perfil;
- alteração de senha somente com hash válido;
- combinação do status da conta e da pessoa para autenticação.

O teste não inicia Spring nem banco. Ele localiza falhas diretamente no domínio, mas não substitui os cenários JWT e HTTP.

## `JogoServiceUnitTest` e Mockito

O novo teste constrói `JogoService` com repositories e `EntityManager` simulados. Cinco cenários protegem riscos de aplicação:

- cadastro com associação correta ao gênero;
- interrupção antecipada para código duplicado;
- ausência de persistência quando o gênero não existe;
- aquisição de lock pessimista antes da entrada de estoque;
- rejeição de saída maior que o saldo sem alterar a quantidade.

Os `when` atuam como stubs ao preparar respostas. As chamadas `verify` são usadas somente quando a interação é parte do comportamento relevante, como adquirir `PESSIMISTIC_WRITE` ou não salvar um cadastro inválido.

No Java 26, o Mockito não conseguiu anexar seu agente dinamicamente. O Surefire passou a iniciar o `mockito-core` explicitamente como `javaagent`. A expressão `@{argLine}` preserva o agente configurado pelo JaCoCo; sem ela, um plugin poderia sobrescrever o outro.

## Teste unitário versus integração

`JogoServiceUnitTest` não inicia Spring e fornece diagnóstico rápido sobre decisões do service. `JogoServiceTest` usa repositories reais, PostgreSQL e transações, comprovando persistência e dirty checking. Eles não são duplicados: um isola decisões e o outro valida a integração que os mocks não conseguem representar.

## JaCoCo e ciclo Maven

O plugin JaCoCo `0.8.16` executa:

```text
prepare-agent → testes → jacoco.exec → report → check
```

O comando reprodutível é:

```powershell
.\mvnw.cmd clean verify
```

`clean` elimina medições residuais. `verify` executa a suíte, gera os relatórios e aplica o portão. O HTML reproduzível fica em `target/site/jacoco/index.html`; como `target/` é ignorado, relatórios de build não entram no Git.

## Medição obtida

| Métrica | Coberto | Total | Percentual |
|---|---:|---:|---:|
| testes aprovados | 92 | 92 | 100% |
| linhas | 943 | 1253 | 75,26% |
| branches | 175 | 352 | 49,72% |
| instruções | 3743 | 5727 | 65,36% |
| métodos | 318 | 468 | 67,95% |
| classes | 84 | 98 | 85,71% |

Alguns recortes relevantes:

| Pacote ou classe | Linhas | Branches | Interpretação |
|---|---:|---:|---|
| `domain` | 88,38% | 73,68% | boa base de regras unitárias |
| `security` | 100% | 100% | pacote pequeno exercitado pelos fluxos JWT |
| `config` | 84,42% | 50% | configurações carregadas por testes Spring |
| `application` | 62,78% | 35,05% | principal área para análise futura por caso de uso |
| `JogoService` | 77,17% | 66,67% | teste unitário complementou a integração existente |
| `ContaAcesso` | 97,73% | 88,89% | decisões críticas de status e perfil bem exercitadas |

Pacotes de interfaces Spring Data aparecem com pouca ou nenhuma linha executável no relatório. Isso não significa que repositories não foram usados; implementações são geradas em tempo de execução pelo Spring.

## Portão de cobertura

Os limites definidos são:

```text
linhas:   70%
branches: 40%
```

Eles ficam abaixo da fotografia atual para detectar regressões relevantes sem transformar o percentual em objetivo absoluto. O pacote de aplicação ainda possui branches não cobertos, mas novos testes devem ser escolhidos pelo impacto da regra, e não apenas para colorir linhas vermelhas.

Foi realizado um experimento controlado com 90% de branches. O build falhou informando cobertura observada de `0.49` contra mínimo `0.90`, comprovando que o portão participa da fase `verify`. O valor foi restaurado para 40%; o limite temporário não faz parte da entrega.

## Cobertura não é qualidade

Uma linha pode aparecer verde mesmo sem asserção. Um teste forte precisa de cenário relevante, oráculo independente, asserção específica, repetibilidade e custo proporcional ao risco. Da mesma forma, 100% de linhas não prova todas as combinações, estados ou efeitos concorrentes.

As propriedades FIRST orientam a suíte:

- **Fast:** testes unitários retornam rapidamente;
- **Independent:** um cenário não prepara o próximo;
- **Repeatable:** profile e banco de testes são explícitos;
- **Self-validating:** aprovação e falha são automáticas;
- **Timely:** o teste é escrito junto da regra, idealmente pelo ciclo TDD.

## TDD

Red–Green–Refactor exige observar primeiro o teste falhar pelo motivo esperado, implementar somente o necessário e depois melhorar o desenho mantendo a suíte verde. Escrever um teste depois da implementação não deve ser chamado de TDD. Nesta aula, a falha causada por um texto esperado incorreto foi tratada como correção de oráculo, não como evidência artificial de uma etapa RED.

## Resultado

O projeto combina testes unitários, Mockito, Spring, PostgreSQL, `MockMvc`, segurança e concorrência. O `clean verify` terminou com 92 testes aprovados, relatório gerado e todos os limites atendidos, sem excluir classes para aumentar percentuais.
