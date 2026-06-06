package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.Casa;
import br.ufpb.dsc.republica.domain.Morador;
import br.ufpb.dsc.republica.domain.StatusTarefa;
import br.ufpb.dsc.republica.domain.Tarefa;
import br.ufpb.dsc.republica.dto.TarefaForm;
import br.ufpb.dsc.republica.repository.CasaRepository;
import br.ufpb.dsc.republica.repository.MoradorRepository;
import br.ufpb.dsc.republica.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final CasaRepository casaRepository;
    private final MoradorRepository moradorRepository;

    public TarefaService(TarefaRepository tarefaRepository, CasaRepository casaRepository, MoradorRepository moradorRepository) {
        this.tarefaRepository = tarefaRepository;
        this.casaRepository = casaRepository;
        this.moradorRepository = moradorRepository;
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
        }

        return tarefaRepository.save(tarefa);
    }

    public Tarefa alterarStatus(Long tarefaId, StatusTarefa status) {
        Tarefa tarefa = buscarPorId(tarefaId);
        tarefa.setStatus(status);
        return tarefaRepository.save(tarefa);
    }

    public Tarefa delegarTarefa(Long tarefaId, Long moradorId) {
        Tarefa tarefa = buscarPorId(tarefaId);
        if (moradorId == null) {
            tarefa.setResponsavel(null);
        } else {
            Morador morador = moradorRepository.findById(moradorId)
                    .orElseThrow(() -> new IllegalArgumentException("Morador não encontrado."));
            if (!morador.getCasa().getId().equals(tarefa.getCasa().getId())) {
                throw new IllegalArgumentException("O responsável deve morar na mesma casa da tarefa.");
            }
            tarefa.setResponsavel(morador);
        }
        return tarefaRepository.save(tarefa);
    }

    public void excluirTarefa(Long tarefaId) {
        Tarefa tarefa = buscarPorId(tarefaId);
        tarefaRepository.delete(tarefa);
    }
}

