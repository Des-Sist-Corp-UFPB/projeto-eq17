package br.ufpb.dsc.republica.dto;

import br.ufpb.dsc.republica.domain.StatusTarefa;

public record TarefaDto(
    Long id,
    String descricao,
    StatusTarefa status,
    MoradorDto responsavel
) {}
