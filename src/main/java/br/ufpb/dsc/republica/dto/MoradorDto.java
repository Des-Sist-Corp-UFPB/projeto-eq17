package br.ufpb.dsc.republica.dto;

import br.ufpb.dsc.republica.domain.PapelMorador;

public record MoradorDto(
    Long id,
    String nome,
    String email,
    PapelMorador papel
) {}
