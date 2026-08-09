package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.LgpdExportDto;
import br.ufpb.dsc.republica.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/meus-dados")
public class LgpdController {

    private final UsuarioService usuarioService;

    public LgpdController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<LgpdExportDto> obterMeusDados(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        LgpdExportDto dados = usuarioService.exportarDados(usuario.getId());
        return ResponseEntity.ok(dados);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> excluirMinhaConta(Principal principal, HttpServletRequest request) throws ServletException {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        usuarioService.excluirUsuario(usuario.getId());

        // Invalida a sessão atual do usuário (realiza o logout automático)
        request.logout();

        return ResponseEntity.ok(Map.of("mensagem", "Sua solicitação de exclusão foi processada. A conta foi removida ou anonimizada com sucesso."));
    }
}
