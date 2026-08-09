package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.Casa;
import br.ufpb.dsc.republica.domain.Morador;
import br.ufpb.dsc.republica.domain.StatusTarefa;
import br.ufpb.dsc.republica.domain.Tarefa;
import br.ufpb.dsc.republica.dto.TarefaForm;
import br.ufpb.dsc.republica.repository.CasaRepository;
import br.ufpb.dsc.republica.repository.MoradorRepository;
import br.ufpb.dsc.republica.repository.TarefaRepository;
import br.ufpb.dsc.republica.domain.TipoNotificacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final CasaRepository casaRepository;
    private final MoradorRepository moradorRepository;
    private final NotificacaoService notificacaoService;
    private final AuditoriaService auditoriaService;

    public TarefaService(TarefaRepository tarefaRepository,
                         CasaRepository casaRepository, 
                         MoradorRepository moradorRepository,
                         NotificacaoService notificacaoService,
                         AuditoriaService auditoriaService) {
        this.tarefaRepository = tarefaRepository;
        this.casaRepository = casaRepository;
        this.moradorRepository = moradorRepository;
        this.notificacaoService = notificacaoService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<Tarefa> buscarTarefasPorCasa(Long casaId) {
        return tarefaRepository.findByCasaIdOrderByCriadoEmDesc(casaId);
    }

    @Transactional(readOnly = true)
    public Tarefa buscarPorId(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada com o ID: " + id));
    }

    public Tarefa criarTarefa(Long casaId, TarefaForm form) {
        Casa casa = casaRepository.findById(casaId)
                .orElseThrow(() -> new IllegalArgumentException("Casa não encontrada."));

        Tarefa tarefa = new Tarefa(casa, form.descricao(), StatusTarefa.PENDENTE);

        if (form.responsavelId() != null) {
            Morador responsavel = moradorRepository.findById(form.responsavelId())
                    .orElseThrow(() -> new IllegalArgumentException("Morador responsável não encontrado."));
            if (!responsavel.getCasa().getId().equals(casaId)) {
                throw new IllegalArgumentException("O responsável deve morar na mesma casa da tarefa.");
            }
            tarefa.setResponsavel(responsavel);
            
            // Notificar o morador responsável
            notificacaoService.criarNotificacao(
                responsavel.getUsuario(),
                "Nova tarefa atribuída a você",
                "Você foi designado como responsável pela tarefa: '" + tarefa.getDescricao() + "'.",
                TipoNotificacao.TAREFA_ATRIBUIDA
            );
        }

        Tarefa tarefaSalva = tarefaRepository.save(tarefa);

        // Auditoria: Criação de tarefa
        String responsavelNome = tarefaSalva.getResponsavel() != null ? tarefaSalva.getResponsavel().getUsuario().getNome() : "Sem responsável";
        auditoriaService.registrarAcaoUsuarioLogado("CRIACAO_TAREFA", 
                String.format("Criou a tarefa '%s' na casa '%s' atribuída a '%s'.", tarefaSalva.getDescricao(), casa.getNome(), responsavelNome), 
                "Tarefa", tarefaSalva.getId());

        return tarefaSalva;
    }

    public Tarefa alterarStatus(Long tarefaId, StatusTarefa status) {
        Tarefa tarefa = buscarPorId(tarefaId);
        StatusTarefa statusAntigo = tarefa.getStatus();
        tarefa.setStatus(status);
        Tarefa tarefaSalva = tarefaRepository.save(tarefa);

        // Auditoria: Conclusão ou Alteração
        String acao = status == StatusTarefa.CONCLUIDA ? "CONCLUSAO_TAREFA" : "ALTERACAO_TAREFA";
        String descricao = status == StatusTarefa.CONCLUIDA ? 
                String.format("Concluiu a tarefa '%s' na casa '%s'.", tarefaSalva.getDescricao(), tarefaSalva.getCasa().getNome()) :
                String.format("Alterou o status da tarefa '%s' de '%s' para '%s'.", tarefaSalva.getDescricao(), statusAntigo.name(), status.name());

        auditoriaService.registrarAcaoUsuarioLogado(acao, descricao, "Tarefa", tarefaId);

        return tarefaSalva;
    }

    public Tarefa delegarTarefa(Long tarefaId, Long moradorId) {
        Tarefa tarefa = buscarPorId(tarefaId);
        String responsavelAntigo = tarefa.getResponsavel() != null ? tarefa.getResponsavel().getUsuario().getNome() : "Ninguém";
        
        if (moradorId == null) {
            tarefa.setResponsavel(null);
            
            // Auditoria: Desatribuição da tarefa
            auditoriaService.registrarAcaoUsuarioLogado("ALTERACAO_TAREFA", 
                    String.format("Removeu a responsabilidade da tarefa '%s' (responsável anterior: %s).", tarefa.getDescricao(), responsavelAntigo), 
                    "Tarefa", tarefaId);
        } else {
            Morador morador = moradorRepository.findById(moradorId)
                    .orElseThrow(() -> new IllegalArgumentException("Morador não encontrado."));
            if (!morador.getCasa().getId().equals(tarefa.getCasa().getId())) {
                throw new IllegalArgumentException("O responsável deve morar na mesma casa da tarefa.");
            }
            tarefa.setResponsavel(morador);
            
            // Notificar o morador responsável
            notificacaoService.criarNotificacao(
                morador.getUsuario(),
                "Tarefa atribuída a você",
                "Você foi designado como responsável pela tarefa: '" + tarefa.getDescricao() + "'.",
                TipoNotificacao.TAREFA_ATRIBUIDA
            );

            // Auditoria: Redelegação da tarefa
            auditoriaService.registrarAcaoUsuarioLogado("ALTERACAO_TAREFA", 
                    String.format("Delegou a responsabilidade da tarefa '%s' para '%s' (responsável anterior: %s).", 
                            tarefa.getDescricao(), morador.getUsuario().getNome(), responsavelAntigo), 
                    "Tarefa", tarefaId);
        }
        return tarefaRepository.save(tarefa);
    }

    public void excluirTarefa(Long tarefaId) {
        Tarefa tarefa = buscarPorId(tarefaId);
        tarefaRepository.delete(tarefa);

        // Auditoria: Exclusão de tarefa
        auditoriaService.registrarAcaoUsuarioLogado("EXCLUSAO_TAREFA", 
                String.format("Excluiu permanentemente a tarefa '%s' na casa '%s'.", tarefa.getDescricao(), tarefa.getCasa().getNome()), 
                "Tarefa", tarefaId);
    }
}
