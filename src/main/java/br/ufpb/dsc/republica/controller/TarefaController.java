package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Morador;
import br.ufpb.dsc.republica.domain.StatusTarefa;
import br.ufpb.dsc.republica.domain.Tarefa;
import br.ufpb.dsc.republica.dto.MoradorDto;
import br.ufpb.dsc.republica.dto.TarefaDto;
import br.ufpb.dsc.republica.dto.TarefaForm;
import br.ufpb.dsc.republica.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST para tarefas.
 */
@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    /**
     * Cadastra uma nova tarefa na casa.
     */
    @PostMapping("/casa/{casaId}")
    public ResponseEntity<TarefaDto> cadastrarTarefa(@PathVariable("casaId") Long casaId,
                                                     @Valid @RequestBody TarefaForm form) {
        Tarefa tarefa = tarefaService.criarTarefa(casaId, form);
        return ResponseEntity.ok(toTarefaDto(tarefa));
    }

    /**
     * Altera o status de uma tarefa.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TarefaDto> alterarStatus(@PathVariable("id") Long id,
                                                   @RequestBody Map<String, String> payload) {
        String statusStr = payload.get("status");
        if (statusStr == null || statusStr.trim().isEmpty()) {
            throw new IllegalArgumentException("O status é obrigatório.");
        }

        StatusTarefa status = StatusTarefa.valueOf(statusStr);
        Tarefa tarefa = tarefaService.alterarStatus(id, status);
        return ResponseEntity.ok(toTarefaDto(tarefa));
    }

    /**
     * Exclui uma tarefa da casa.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> excluirTarefa(@PathVariable("id") Long id) {
        tarefaService.excluirTarefa(id);
        return ResponseEntity.ok(Map.of("mensagem", "Tarefa excluída com sucesso."));
    }

    private TarefaDto toTarefaDto(Tarefa t) {
        return new TarefaDto(
                t.getId(),
                t.getDescricao(),
                t.getStatus(),
                toMoradorDto(t.getResponsavel())
        );
    }

    private MoradorDto toMoradorDto(Morador m) {
        if (m == null) {
            return null;
        }
        return new MoradorDto(
                m.getId(),
                m.getUsuario().getNome(),
                m.getUsuario().getEmail(),
                m.getPapel()
        );
    }
}


