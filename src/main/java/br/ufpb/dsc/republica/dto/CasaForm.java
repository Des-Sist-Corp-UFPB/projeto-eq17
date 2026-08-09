package br.ufpb.dsc.republica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CasaForm(
    @NotBlank(message = "O nome da casa/república é obrigatório")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
    String nome,

    @NotBlank(message = "O endereço é obrigatório")
    String endereco
) {}

