package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.*;
import br.ufpb.dsc.republica.repository.DespesaRepository;
import br.ufpb.dsc.republica.repository.NotificacaoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final DespesaRepository despesaRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, DespesaRepository despesaRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.despesaRepository = despesaRepository;
    }

    /**
     * Cria e salva uma nova notificação para um usuário.
     */
    public Notificacao criarNotificacao(Usuario usuario, String titulo, String mensagem, TipoNotificacao tipo) {
        Notificacao notificacao = new Notificacao(usuario, titulo, mensagem, tipo);
        return notificacaoRepository.save(notificacao);
    }

    /**
     * Retorna todas as notificações de um usuário ordenadas pela data de criação.
     */
    @Transactional(readOnly = true)
    public List<Notificacao> buscarNotificacoesDoUsuario(Long usuarioId) {
        return notificacaoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId);
    }

    /**
     * Conta a quantidade de notificações não lidas de um usuário.
     */
    @Transactional(readOnly = true)
    public long contarNaoLidas(Long usuarioId) {
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    /**
     * Marca uma notificação específica como lida se ela pertencer ao usuário.
     */
    public void marcarComoLida(Long notificacaoId, Long usuarioId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada."));

        if (!notificacao.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("Esta notificação não pertence ao usuário.");
        }

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    /**
     * Marca todas as notificações do usuário como lidas.
     */
    public void marcarTodasComoLidas(Long usuarioId) {
        notificacaoRepository.marcarTodasComoLidas(usuarioId);
    }

    /**
     * Agendamento: Roda todos os dias às 8:00h da manhã buscando despesas não pagas
     * que vencem no dia seguinte (amanhã) e gera notificações para os moradores.
     * Cron: "0 0 8 * * ?" (Às 8:00h AM diariamente)
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void enviarNotificacoesVencimento() {
        LocalDate amanha = LocalDate.now().plusDays(1);
        List<Despesa> despesasAmanha = despesaRepository.findByExcluidoFalseAndStatusNotAndVencimento(StatusDespesa.PAGA, amanha);

        for (Despesa despesa : despesasAmanha) {
            for (DespesaRateio rateio : despesa.getRateios()) {
                // Se o pagamento do rateio ainda não foi confirmado
                boolean pago = rateio.getPagamentos().stream()
                        .anyMatch(p -> p.getStatus() == StatusPagamento.CONFIRMADO);

                if (!pago) {
                    Usuario usuario = rateio.getMorador().getUsuario();
                    String titulo = "Despesa vencendo amanhã!";
                    String pixMsg = despesa.getChavePix() != null ? " (PIX para pagamento: " + despesa.getChavePix() + ")" : "";
                    String mensagem = String.format("A despesa '%s' vence amanhã (%s). Sua parte devida é R$ %.2f%s.",
                            despesa.getDescricao(),
                            despesa.getVencimento(),
                            rateio.getValorDevido(),
                            pixMsg
                    );
                    
                    criarNotificacao(usuario, titulo, mensagem, TipoNotificacao.VENCIMENTO_PROXIMO);
                }
            }
        }
    }
}
