package br.ufpb.dsc.republica.dto;

import java.time.Instant;

public record CasaDto(
    Long id,
    String nome,
    String endereco,
    Instant criadoEm
) {}
