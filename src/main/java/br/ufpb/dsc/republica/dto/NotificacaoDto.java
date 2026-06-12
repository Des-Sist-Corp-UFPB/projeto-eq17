package br.ufpb.dsc.republica.dto;

import br.ufpb.dsc.republica.domain.TipoNotificacao;
import java.time.Instant;

public record NotificacaoDto(
    Long id,
    String titulo,
    String mensagem,
    TipoNotificacao tipo,
    Boolean lida,
    Instant criadoEm
) {}
