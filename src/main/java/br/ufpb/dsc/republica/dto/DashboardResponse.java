package br.ufpb.dsc.republica.dto;

import java.util.List;

public record DashboardResponse(
    UsuarioDto usuarioLogado,
    List<CasaDto> casas
) {}
