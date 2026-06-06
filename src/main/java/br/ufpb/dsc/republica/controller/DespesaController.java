package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Casa;
import br.ufpb.dsc.republica.domain.Despesa;
import br.ufpb.dsc.republica.domain.DespesaRateio;
import br.ufpb.dsc.republica.domain.Morador;
import br.ufpb.dsc.republica.domain.Pagamento;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.DespesaForm;
import br.ufpb.dsc.republica.repository.DespesaRateioRepository;
import br.ufpb.dsc.republica.repository.PagamentoRepository;
import br.ufpb.dsc.republica.service.CasaService;
import br.ufpb.dsc.republica.service.DespesaService;
import br.ufpb.dsc.republica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/despesas")
public class DespesaController {

    private final DespesaService despesaService;
    private final CasaService casaService;
    private final UsuarioService usuarioService;
    private final DespesaRateioRepository despesaRateioRepository;
    private final PagamentoRepository pagamentoRepository;

    public DespesaController(DespesaService despesaService, CasaService casaService, 
                             UsuarioService usuarioService, DespesaRateioRepository despesaRateioRepository, 
                             PagamentoRepository pagamentoRepository) {
        this.despesaService = despesaService;
        this.casaService = casaService;
        this.usuarioService = usuarioService;
        this.despesaRateioRepository = despesaRateioRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @PostMapping("/casa/{casaId}")
    public String cadastrarDespesa(@PathVariable("casaId") Long casaId,
                                   @Valid @ModelAttribute("despesaForm") DespesaForm form,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/casas/" + casaId + "?erroCadastroDespesa";
        }

        try {
            despesaService.cadastrarDespesa(casaId, form);
            return "redirect:/casas/" + casaId;
        } catch (Exception e) {
            return "redirect:/casas/" + casaId + "?erro=" + e.getMessage();
        }
    }

    // Retorna os rateios de uma despesa via HTMX para abrir no modal de detalhes
    @GetMapping("/{id}/rateios")
    public String verRateios(@PathVariable("id") Long id, Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        Despesa despesa = despesaService.buscarPorId(id);
        Morador moradorLogado = casaService.buscarMoradorPorUsuarioECasa(despesa.getCasa().getId(), usuario.getId());

        model.addAttribute("despesa", despesa);
        model.addAttribute("moradorLogado", moradorLogado);

        return "fragments/republica_fragments :: detalhes-despesa-modal";
    }

    // Registrar pagamento do rateio (feito pelo morador via HTMX)
    @PostMapping("/rateio/{rateioId}/pagar-htmx")
    public String registrarPagamentoHtmx(@PathVariable("rateioId") Long rateioId,
                                         @RequestParam("comprovante") String comprovante,
                                         Model model,
                                         Principal principal) {
        despesaService.informarPagamento(rateioId, comprovante);

        // Recarrega os rateios da despesa associada
        Despesa despesa = despesaService.buscarPorId(idDaDespesaDoRateio(rateioId));
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElseThrow();
        Morador moradorLogado = casaService.buscarMoradorPorUsuarioECasa(despesa.getCasa().getId(), usuario.getId());

        model.addAttribute("despesa", despesa);
        model.addAttribute("moradorLogado", moradorLogado);

        return "fragments/republica_fragments :: detalhes-despesa-corpo";
    }

    @PostMapping("/pagamento/{pagamentoId}/confirmar")
    public String confirmarPagamento(@PathVariable("pagamentoId") Long pagamentoId,
                                     Model model,
                                     Principal principal) {
        despesaService.confirmarPagamento(pagamentoId);

        Despesa despesa = despesaService.buscarPorId(idDaDespesaDoPagamento(pagamentoId));
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElseThrow();
        Morador moradorLogado = casaService.buscarMoradorPorUsuarioECasa(despesa.getCasa().getId(), usuario.getId());

        model.addAttribute("despesa", despesa);
        model.addAttribute("moradorLogado", moradorLogado);

        return "fragments/republica_fragments :: detalhes-despesa-corpo";
    }

    @PostMapping("/pagamento/{pagamentoId}/rejeitar")
    public String rejeitarPagamento(@PathVariable("pagamentoId") Long pagamentoId,
                                    Model model,
                                    Principal principal) {
        despesaService.rejeitarPagamento(pagamentoId);

        Despesa despesa = despesaService.buscarPorId(idDaDespesaDoPagamento(pagamentoId));
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElseThrow();
        Morador moradorLogado = casaService.buscarMoradorPorUsuarioECasa(despesa.getCasa().getId(), usuario.getId());

        model.addAttribute("despesa", despesa);
        model.addAttribute("moradorLogado", moradorLogado);

        return "fragments/republica_fragments :: detalhes-despesa-corpo";
    }

    // Exclusão lógica
    @PostMapping("/{despesaId}/excluir")
    public String excluirDespesa(@PathVariable("despesaId") Long despesaId,
                                 Model model,
                                 Principal principal) {
        Despesa despesa = despesaService.buscarPorId(despesaId);
        Long casaId = despesa.getCasa().getId();
        despesaService.excluirDespesa(despesaId);

        // Retorna a tabela atualizada de despesas
        List<Despesa> despesas = despesaService.buscarDespesasPorCasa(casaId);
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName()).orElseThrow();
        Morador moradorLogado = casaService.buscarMoradorPorUsuarioECasa(casaId, usuario.getId());

        model.addAttribute("despesas", despesas);
        model.addAttribute("moradorLogado", moradorLogado);

        return "fragments/republica_fragments :: lista-despesas";
    }

    // Métodos auxiliares para buscar despesa
    private Long idDaDespesaDoRateio(Long rateioId) {
        return despesaRateioRepository.findById(rateioId)
                .map(r -> r.getDespesa().getId())
                .orElseThrow(() -> new IllegalArgumentException("Rateio não encontrado."));
    }

    private Long idDaDespesaDoPagamento(Long pagamentoId) {
        return pagamentoRepository.findById(pagamentoId)
                .map(p -> p.getRateio().getDespesa().getId())
                .orElseThrow(() -> new IllegalArgumentException("Pagamento não encontrado."));
    }
}

