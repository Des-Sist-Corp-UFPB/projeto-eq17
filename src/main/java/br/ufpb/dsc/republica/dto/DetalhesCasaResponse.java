package br.ufpb.dsc.republica.dto;

import java.util.List;

public record DetalhesCasaResponse(
    CasaDto casa,
    MoradorDto moradorLogado,
    List<MoradorDto> moradores,
    List<DespesaDto> despesas,
    List<TarefaDto> tarefas
) {}
