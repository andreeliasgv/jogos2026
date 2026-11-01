package com.curso.jogos.api.dto;

import com.curso.jogos.api.validation.CnpjValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar um fornecedor de jogos")
public record FornecedorRequest(
        @NotBlank(message = "Razão social é obrigatória")
        @Size(max = 150, message = "Razão social deve possuir no máximo 150 caracteres")
        @Schema(example = "Distribuidora Aurora Ltda.") String razaoSocial,
        @NotBlank(message = "CNPJ é obrigatório") @CnpjValido
        @Schema(example = "12345678000195") String cnpj) {
}
