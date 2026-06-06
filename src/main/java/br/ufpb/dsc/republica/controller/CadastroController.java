package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.dto.UsuarioForm;
import br.ufpb.dsc.republica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cadastro")
public class CadastroController {

    private final UsuarioService usuarioService;

    public CadastroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String exibirFormulario(Model model) {
        model.addAttribute("usuarioForm", new UsuarioForm("", "", ""));
        return "auth/cadastro";
    }

    @PostMapping
    public String cadastrar(@Valid @ModelAttribute("usuarioForm") UsuarioForm form,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/cadastro";
        }

        try {
            usuarioService.cadastrar(form);
            return "redirect:/login?cadastroSucesso";
        } catch (IllegalArgumentException e) {
            model.addAttribute("cadastroErro", e.getMessage());
            return "auth/cadastro";
        }
    }
}

