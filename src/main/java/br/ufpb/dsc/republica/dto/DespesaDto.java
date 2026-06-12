package br.ufpb.dsc.republica.dto;

import br.ufpb.dsc.republica.domain.StatusDespesa;
import br.ufpb.dsc.republica.domain.TipoDespesa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DespesaDto(
    Long id,
    String descricao,
    BigDecimal valorTotal,
    LocalDate vencimento,
    StatusDespesa status,
    MoradorDto responsavel,
    List<DespesaRateioDto> rateios,
    TipoDespesa tipo,
    String chavePix
) {}
