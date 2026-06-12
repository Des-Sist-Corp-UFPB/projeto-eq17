package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.UsuarioDto;
import br.ufpb.dsc.republica.dto.UsuarioForm;
import br.ufpb.dsc.republica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de cadastro REST.
 */
@RestController
@RequestMapping("/api/auth/register")
public class CadastroController {

    private final UsuarioService usuarioService;

    public CadastroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Cadastra um novo usuário no sistema.
     */
    @PostMapping
    public ResponseEntity<UsuarioDto> cadastrar(@Valid @RequestBody UsuarioForm form) {
        Usuario usuario = usuarioService.cadastrar(form);
        return ResponseEntity.ok(new UsuarioDto(usuario.getId(), usuario.getNome(), usuario.getEmail()));
    }
}


