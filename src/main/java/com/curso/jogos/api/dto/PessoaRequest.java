package com.curso.jogos.api.dto;

import com.curso.jogos.api.validation.CpfValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Identidade compartilhada pelos papéis de cliente e colaborador")
public record PessoaRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve possuir no máximo 150 caracteres")
        @Schema(example = "Ana Souza") String nome,
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 180, message = "E-mail deve possuir no máximo 180 caracteres")
        @Schema(example = "ana.souza@example.com") String email,
        @NotBlank(message = "CPF é obrigatório")
        @CpfValido
        @Schema(example = "52998224725") String cpf) {
}
