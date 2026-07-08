package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.service.UsuarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/auth/confirmar-email")
public class EmailConfirmacaoController {

    private final UsuarioService usuarioService;

    public EmailConfirmacaoController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public RedirectView confirmar(@RequestParam("token") String token) {
        try {
            usuarioService.confirmarEmail(token);
            return new RedirectView("/login?confirmed=true");
        } catch (IllegalArgumentException e) {
            return new RedirectView("/login?confirmed=false&error=" + e.getMessage());
        }
    }
}
