package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.EsqueceuSenhaForm;
import br.ufpb.dsc.republica.dto.RedefinirSenhaForm;
import br.ufpb.dsc.republica.dto.UsuarioDto;
import br.ufpb.dsc.republica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

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

    /**
     * Solicita o envio do e-mail de redefinição de senha.
     */
    @PostMapping("/esqueceu-senha")
    public ResponseEntity<Map<String, String>> solicitarRedefinicaoSenha(@Valid @RequestBody EsqueceuSenhaForm form) {
        usuarioService.solicitarRedefinicaoSenha(form.email());
        return ResponseEntity.ok(Map.of("mensagem", "Se o e-mail estiver cadastrado em nosso sistema, você receberá um link com as instruções para redefinir sua senha."));
    }

    /**
     * Valida se um token de redefinição de senha ainda é válido.
     */
    @GetMapping("/validar-token-redefinicao")
    public ResponseEntity<Map<String, Boolean>> validarTokenRedefinicao(@RequestParam("token") String token) {
        boolean valido = usuarioService.validarTokenRedefinicao(token);
        return ResponseEntity.ok(Map.of("valido", valido));
    }

    /**
     * Redefine a senha utilizando o token de recuperação.
     */
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@Valid @RequestBody RedefinirSenhaForm form) {
        usuarioService.redefinirSenha(form.token(), form.novaSenha());
        return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso! Você já pode realizar login com sua nova senha."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
    }
}


