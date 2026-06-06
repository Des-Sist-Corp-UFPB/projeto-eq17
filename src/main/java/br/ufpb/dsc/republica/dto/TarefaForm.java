package br.ufpb.dsc.republica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TarefaForm(
    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 255, message = "A descrição pode ter no máximo 255 caracteres")
    String descricao,

    Long responsavelId
) {}

