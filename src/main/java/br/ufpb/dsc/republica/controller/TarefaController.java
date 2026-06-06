package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Morador;
import br.ufpb.dsc.republica.domain.StatusTarefa;
import br.ufpb.dsc.republica.domain.Tarefa;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.TarefaForm;
import br.ufpb.dsc.republica.service.CasaService;
import br.ufpb.dsc.republica.service.TarefaService;
import br.ufpb.dsc.republica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;
    private final CasaService casaService;
    private final UsuarioService usuarioService;

    public TarefaController(TarefaService tarefaService, CasaService casaService, UsuarioService usuarioService) {
        this.tarefaService = tarefaService;
        this.casaService = casaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/casa/{casaId}")
    public String cadastrarTarefa(@PathVariable("casaId") Long casaId,
                                  @Valid @ModelAttribute("tarefaForm") TarefaForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  Principal principal) {
        if (!bindingResult.hasErrors()) {
            try {
                tarefaService.criarTarefa(casaId, form);
            } catch (Exception e) {
                model.addAttribute("erroTarefa", e.getMessage());
            }
        }

        // Retorna a lista atualizada de tarefas via fragmento
        List<Tarefa> tarefas = tarefaService.buscarTarefasPorCasa(casaId);
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElseThrow();
        Morador moradorLogado = casaService.buscarMoradorPorUsuarioECasa(casaId, usuario.getId());

        model.addAttribute("tarefas", tarefas);
        model.addAttribute("moradorLogado", moradorLogado);

        return "fragments/republica_fragments :: lista-tarefas";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable("id") Long id,
                                @RequestParam("status") StatusTarefa status,
                                Model model,
                                Principal principal) {
        Tarefa tarefa = tarefaService.alterarStatus(id, status);
        Long casaId = tarefa.getCasa().getId();

        // Retorna a lista atualizada de tarefas
        List<Tarefa> tarefas = tarefaService.buscarTarefasPorCasa(casaId);
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElseThrow();
        Morador moradorLogado = casaService.buscarMoradorPorUsuarioECasa(casaId, usuario.getId());

        model.addAttribute("tarefas", tarefas);
        model.addAttribute("moradorLogado", moradorLogado);

        return "fragments/republica_fragments :: lista-tarefas";
    }

    @PostMapping("/{id}/excluir")
    public String excluirTarefa(@PathVariable("id") Long id,
                                Model model,
                                Principal principal) {
        Tarefa tarefa = tarefaService.buscarPorId(id);
        Long casaId = tarefa.getCasa().getId();
        
        tarefaService.excluirTarefa(id);

        // Retorna a lista atualizada de tarefas
        List<Tarefa> tarefas = tarefaService.buscarTarefasPorCasa(casaId);
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElseThrow();
        Morador moradorLogado = casaService.buscarMoradorPorUsuarioECasa(casaId, usuario.getId());

        model.addAttribute("tarefas", tarefas);
        model.addAttribute("moradorLogado", moradorLogado);

        return "fragments/republica_fragments :: lista-tarefas";
    }
}

