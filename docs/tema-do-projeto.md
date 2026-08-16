# Jogos 2026

API desenvolvida durante a disciplina de Laboratório de Programação IV.

## Tema

API para controle de jogos de uma loja, organizados por gênero.

## Identificação

- Nome do projeto: `jogos2026`
- Tema: controle simplificado de jogos de uma loja didática
- Objetivo em uma frase: cadastrar jogos, organizá-los por gênero e controlar sua quantidade disponível e preço.

## Entidade de classificação

- Nome no singular: `GeneroJogo`
- Nome no plural: gêneros de jogos
- Descrição: classificação utilizada para organizar os jogos
- Exemplo 1: Aventura
- Exemplo 2: Estratégia
- Status: ativo ou inativo

## Entidade principal

- Nome no singular: `Jogo`
- Nome no plural: jogos
- Código único: código do jogo
- Descrição: título do jogo
- Medida quantitativa: quantidade de unidades disponíveis
- Valor monetário: preço unitário
- Valor calculado: quantidade disponível multiplicada pelo preço unitário
- Data relevante: data de cadastro
- Status: ativo ou inativo

## Relacionamento

- Um gênero de jogo pode classificar vários jogos.
- Cada jogo pertence a um gênero de jogo.

## Três exemplos de registros

1. Aventura — `JG-001` — Ruínas de Aurion — 12 unidades — R$ 79,90 — ativo.
2. Estratégia — `JG-002` — Reinos de Ferro — 8 unidades — R$ 119,90 — ativo.
3. Corrida — `JG-003` — Velocidade Neon — 15 unidades — R$ 59,90 — ativo.