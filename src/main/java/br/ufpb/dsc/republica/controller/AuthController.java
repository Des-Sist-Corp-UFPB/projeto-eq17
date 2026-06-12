package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.UsuarioDto;
import br.ufpb.dsc.republica.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Controller de autenticação REST.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Retorna as informações do usuário autenticado na sessão atual.
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioDto> me(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        return ResponseEntity.ok(new UsuarioDto(usuario.getId(), usuario.getNome(), usuario.getEmail()));
    }
}

