package br.ufpb.dsc.republica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaForm(
        @NotBlank(message = "O token é obrigatório")
        String token,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 6, max = 100, message = "A nova senha deve ter entre 6 e 100 caracteres")
        String novaSenha
) {
}
