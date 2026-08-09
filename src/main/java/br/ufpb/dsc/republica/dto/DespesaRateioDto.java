package br.ufpb.dsc.republica.dto;

import br.ufpb.dsc.republica.domain.StatusPagamento;
import java.math.BigDecimal;
import java.time.Instant;

public record DespesaRateioDto(
    Long id,
    MoradorDto morador,
    BigDecimal valorDevido,
    StatusPagamento statusPagamento,
    Long pagamentoId,
    String comprovante,
    Instant dataPagamento
) {}
