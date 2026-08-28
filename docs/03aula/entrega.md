# Aula 03 — Modelo de domínio

## Correspondência

| Projeto de referência | Projeto de jogos |
|---|---|
| GrupoProduto | GeneroJogo |
| Produto | Jogo |
| codigoBarras | codigoJogo |
| descricao | titulo |
| saldoEstoque | quantidadeDisponivel |
| valorUnitario | precoUnitario |
| calcularValorEstoque | calcularValorInventario |

## Decisões de tipos

A quantidade disponível utiliza `int` porque jogos são unidades
físicas indivisíveis.

O preço unitário utiliza `BigDecimal` para evitar imprecisão em
cálculos monetários.

A data de cadastro utiliza `LocalDate` porque o horário não faz
parte da regra atual.

## Invariantes

1. Um jogo nunca pode possuir quantidade negativa.
2. Movimentações devem possuir quantidade maior que zero.
3. Não é possível retirar mais unidades do que estão disponíveis.
4. Um jogo não pode pertencer a dois gêneros.
5. Um gênero não aceita dois jogos com o mesmo código.
6. A lista interna de jogos não pode ser alterada diretamente.

## Associação

A associação é iniciada por `GeneroJogo.adicionarJogo`. Esse método
atualiza a coleção do gênero e a referência existente no jogo,
mantendo os dois lados consistentes.

## Cálculo

O valor do inventário é calculado multiplicando a quantidade
disponível pelo preço unitário.