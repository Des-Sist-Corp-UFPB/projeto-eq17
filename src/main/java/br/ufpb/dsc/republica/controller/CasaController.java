package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Casa;
import br.ufpb.dsc.republica.domain.Despesa;
import br.ufpb.dsc.republica.domain.Morador;
import br.ufpb.dsc.republica.domain.Tarefa;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.CasaForm;
import br.ufpb.dsc.republica.dto.DespesaForm;
import br.ufpb.dsc.republica.dto.TarefaForm;
import br.ufpb.dsc.republica.service.CasaService;
import br.ufpb.dsc.republica.service.DespesaService;
import br.ufpb.dsc.republica.service.TarefaService;
import br.ufpb.dsc.republica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/casas")
public class CasaController {

    private final CasaService casaService;
    private final DespesaService despesaService;
    private final TarefaService tarefaService;
    private final UsuarioService usuarioService;

    public CasaController(CasaService casaService, DespesaService despesaService,
                          TarefaService tarefaService, UsuarioService usuarioService) {
        this.casaService = casaService;
        this.despesaService = despesaService;
        this.tarefaService = tarefaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public String criarCasa(@Valid @ModelAttribute("casaForm") CasaForm form,
                            BindingResult bindingResult,
                            Principal principal,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "redirect:/dashboard?erroCriarCasa";
        }

        try {
            Casa casa = casaService.criarCasa(form, principal.getName());
            return "redirect:/casas/" + casa.getId();
        } catch (Exception e) {
            return "redirect:/dashboard?erro=" + e.getMessage();
        }
    }

    @GetMapping("/{id}")
    public String exibirDetalhes(@PathVariable("id") Long id, Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        Casa casa = casaService.buscarPorId(id);
        Morador moradorLogado = casaService.buscarMoradorPorUsuarioECasa(casa.getId(), usuario.getId());

        List<Morador> moradores = casaService.buscarMoradores(id);
        List<Despesa> despesas = despesaService.buscarDespesasPorCasa(id);
        List<Tarefa> tarefas = tarefaService.buscarTarefasPorCasa(id);

        model.addAttribute("casa", casa);
        model.addAttribute("moradorLogado", moradorLogado);
        model.addAttribute("moradores", moradores);
        model.addAttribute("despesas", despesas);
        model.addAttribute("tarefas", tarefas);

        // Formulários em branco para os modais
        model.addAttribute("despesaForm", new DespesaForm("", BigDecimal.ZERO, LocalDate.now(), 0L));
        model.addAttribute("tarefaForm", new TarefaForm("", 0L));

        return "casa_detalhes";
    }

    // Endpoint HTMX para convidar morador
    @PostMapping("/{id}/moradores")
    public String convidarMorador(@PathVariable("id") Long id,
                                  @RequestParam("email") String email,
                                  Model model) {
        try {
            casaService.adicionarMorador(id, email);
            
            // Retorna a lista atualizada de moradores via fragmento
            List<Morador> moradores = casaService.buscarMoradores(id);
            model.addAttribute("moradores", moradores);
            model.addAttribute("conviteSucesso", "Morador adicionado com sucesso!");
        } catch (Exception e) {
            List<Morador> moradores = casaService.buscarMoradores(id);
            model.addAttribute("moradores", moradores);
            model.addAttribute("conviteErro", e.getMessage());
        }

        return "fragments/republica_fragments :: lista-moradores";
    }
}

