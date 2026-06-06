package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Casa;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.CasaForm;
import br.ufpb.dsc.republica.service.CasaService;
import br.ufpb.dsc.republica.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final CasaService casaService;
    private final UsuarioService usuarioService;

    public DashboardController(CasaService casaService, UsuarioService usuarioService) {
        this.casaService = casaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String exibirDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        List<Casa> casas = casaService.buscarCasasPorUsuario(usuario.getId());

        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("casas", casas);
        model.addAttribute("casaForm", new CasaForm("", ""));

        return "dashboard";
    }
}

