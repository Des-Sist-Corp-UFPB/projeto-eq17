package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/confirmar-email")
public class EmailConfirmacaoController {

    private final UsuarioService usuarioService;

    public EmailConfirmacaoController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> confirmar(@RequestParam("token") String token) {
        usuarioService.confirmarEmail(token);
        return ResponseEntity.ok(Map.of("mensagem", "E-mail confirmado com sucesso! Sua conta está ativa para login."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
    }
}
