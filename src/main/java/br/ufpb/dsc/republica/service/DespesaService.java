package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.*;
import br.ufpb.dsc.republica.dto.DespesaForm;
import br.ufpb.dsc.republica.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DespesaService {

    private final DespesaRepository despesaRepository;
    private final DespesaRateioRepository despesaRateioRepository;
    private final PagamentoRepository pagamentoRepository;
    private final CasaRepository casaRepository;
    private final MoradorRepository moradorRepository;

    public DespesaService(DespesaRepository despesaRepository,
                          DespesaRateioRepository despesaRateioRepository,
                          PagamentoRepository pagamentoRepository,
                          CasaRepository casaRepository,
                          MoradorRepository moradorRepository) {
        this.despesaRepository = despesaRepository;
        this.despesaRateioRepository = despesaRateioRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.casaRepository = casaRepository;
        this.moradorRepository = moradorRepository;
    }

    @Transactional(readOnly = true)
    public List<Despesa> buscarDespesasPorCasa(Long casaId) {
        return despesaRepository.findByCasaIdAndExcluidoFalseOrderByVencimentoAsc(casaId);
    }

    @Transactional(readOnly = true)
    public Despesa buscarPorId(Long id) {
        Despesa despesa = despesaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Despesa não encontrada com o ID: " + id));
        if (despesa.getExcluido()) {
            throw new IllegalArgumentException("Esta despesa foi removida.");
        }
        return despesa;
    }

    public Despesa cadastrarDespesa(Long casaId, DespesaForm form) {
        Casa casa = casaRepository.findById(casaId)
                .orElseThrow(() -> new IllegalArgumentException("Casa não encontrada."));

        Morador responsavel = moradorRepository.findById(form.responsavelId())
                .orElseThrow(() -> new IllegalArgumentException("Responsável não encontrado."));

        if (!responsavel.getCasa().getId().equals(casaId)) {
            throw new IllegalArgumentException("O responsável deve ser morador da mesma casa.");
        }

        List<Morador> moradores = moradorRepository.findByCasaId(casaId);
        if (moradores.isEmpty()) {
            throw new IllegalArgumentException("A casa deve ter pelo menos um morador para registrar despesas.");
        }

        Despesa despesa = new Despesa(casa, form.descricao(), form.valorTotal(), form.vencimento(), responsavel, StatusDespesa.PENDENTE, form.tipo(), form.chavePix());
        despesa = despesaRepository.save(despesa);

        // Divisão automática e matemática exata do rateio
        int numMoradores = moradores.size();
        BigDecimal valorTotal = form.valorTotal();
        BigDecimal valorBase = valorTotal.divide(BigDecimal.valueOf(numMoradores), 2, RoundingMode.DOWN);
        BigDecimal somaBase = valorBase.multiply(BigDecimal.valueOf(numMoradores));
        BigDecimal diferenca = valorTotal.subtract(somaBase);

        List<DespesaRateio> rateios = new ArrayList<>();
        for (int i = 0; i < numMoradores; i++) {
            Morador morador = moradores.get(i);
            BigDecimal valorDevido = valorBase;
            
            // Adiciona a diferença de centavos ao primeiro morador da lista
            if (i == 0) {
                valorDevido = valorDevido.add(diferenca);
            }

            DespesaRateio rateio = new DespesaRateio(despesa, morador, valorDevido);
            rateio = despesaRateioRepository.save(rateio);

            // Cria o registro do pagamento pendente associado ao rateio
            Pagamento pagamento = new Pagamento(rateio, StatusPagamento.PENDENTE);
            pagamentoRepository.save(pagamento);

            rateio.getPagamentos().add(pagamento);
            rateios.add(rateio);
        }

        despesa.setRateios(rateios);
        return despesa;
    }

    public void informarPagamento(Long rateioId, String comprovante) {
        DespesaRateio rateio = despesaRateioRepository.findById(rateioId)
                .orElseThrow(() -> new IllegalArgumentException("Rateio não encontrado."));

        // Cria ou atualiza o pagamento associado
        Pagamento pagamento = rateio.getUltimoPagamento();
        if (pagamento == null || pagamento.getStatus() == StatusPagamento.CONFIRMADO) {
            pagamento = new Pagamento(rateio, StatusPagamento.INFORMADO);
        } else {
            pagamento.setStatus(StatusPagamento.INFORMADO);
        }

        pagamento.setDataPagamento(Instant.now());
        pagamento.setComprovante(comprovante);
        pagamentoRepository.save(pagamento);

        // Atualiza status da Despesa
        atualizarStatusDespesa(rateio.getDespesa());
    }

    public void confirmarPagamento(Long pagamentoId) {
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado."));

        pagamento.setStatus(StatusPagamento.CONFIRMADO);
        pagamentoRepository.save(pagamento);

        // Atualiza status da Despesa
        atualizarStatusDespesa(pagamento.getRateio().getDespesa());
    }

    public void rejeitarPagamento(Long pagamentoId) {
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado."));

        pagamento.setStatus(StatusPagamento.REJEITADO);
        pagamentoRepository.save(pagamento);

        // Atualiza status da Despesa
        atualizarStatusDespesa(pagamento.getRateio().getDespesa());
    }

    public void excluirDespesa(Long despesaId) {
        Despesa despesa = buscarPorId(despesaId);
        despesa.setExcluido(true);
        despesaRepository.save(despesa);
    }

    private void atualizarStatusDespesa(Despesa despesa) {
        List<DespesaRateio> rateios = despesa.getRateios();
        
        boolean todosConfirmados = true;
        boolean algumConfirmadoOuInformado = false;

        for (DespesaRateio rateio : rateios) {
            Pagamento pag = rateio.getUltimoPagamento();
            if (pag == null || pag.getStatus() != StatusPagamento.CONFIRMADO) {
                todosConfirmados = false;
            }
            if (pag != null && (pag.getStatus() == StatusPagamento.CONFIRMADO || pag.getStatus() == StatusPagamento.INFORMADO)) {
                algumConfirmadoOuInformado = true;
            }
        }

        if (todosConfirmados) {
            despesa.setStatus(StatusDespesa.PAGA);
        } else if (algumConfirmadoOuInformado) {
            despesa.setStatus(StatusDespesa.PARCIALMENTE_PAGA);
        } else {
            despesa.setStatus(StatusDespesa.PENDENTE);
        }

        despesaRepository.save(despesa);
    }
}

