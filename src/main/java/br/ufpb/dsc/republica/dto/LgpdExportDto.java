package br.ufpb.dsc.republica.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record LgpdExportDto(
    UsuarioDados cadastral,
    List<CasaDados> casas,
    List<DespesaDados> despesas,
    List<PagamentoDados> pagamentos,
    List<TarefaDados> tarefas,
    List<AuditoriaDados> auditorias
) {
    public record UsuarioDados(
        Long id,
        String nome,
        String email,
        Instant criadoEm,
        Boolean aceitouTermosLgpd,
        Instant dataAceiteLgpd,
        String versaoTermoLgpd
    ) {}

    public record CasaDados(
        Long id,
        String nome,
        String endereco,
        String papel,
        Instant criadoEm
    ) {}

    public record DespesaDados(
        Long id,
        Long casaId,
        String casaNome,
        String descricao,
        BigDecimal valorTotal,
        LocalDate vencimento,
        String status,
        String tipo,
        String chavePix,
        Instant criadoEm
    ) {}

    public record PagamentoDados(
        Long id,
        Long rateioId,
        Long despesaId,
        String despesaDescricao,
        BigDecimal valorDevido,
        Instant dataPagamento,
        String comprovante,
        String status,
        Instant criadoEm
    ) {}

    public record TarefaDados(
        Long id,
        Long casaId,
        String casaNome,
        String descricao,
        String status,
        Instant criadoEm,
        Instant atualizadoEm
    ) {}

    public record AuditoriaDados(
        Long id,
        String acao,
        String descricao,
        Instant dataHora,
        String enderecoIp,
        String entidadeAfetada,
        Long entidadeId
    ) {}
}
