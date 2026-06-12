package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Notificacao;
import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.NotificacaoDto;
import br.ufpb.dsc.republica.service.NotificacaoService;
import br.ufpb.dsc.republica.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final UsuarioService usuarioService;

    public NotificacaoController(NotificacaoService notificacaoService, UsuarioService usuarioService) {
        this.notificacaoService = notificacaoService;
        this.usuarioService = usuarioService;
    }

    /**
     * Retorna todas as notificações do usuário logado.
     */
    @GetMapping
    public ResponseEntity<List<NotificacaoDto>> listarNotificacoes(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        List<Notificacao> notificacoes = notificacaoService.buscarNotificacoesDoUsuario(usuario.getId());
        List<NotificacaoDto> dtos = notificacoes.stream()
                .map(n -> new NotificacaoDto(n.getId(), n.getTitulo(), n.getMensagem(), n.getTipo(), n.getLida(), n.getCriadoEm()))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * Retorna a quantidade de notificações não lidas.
     */
    @GetMapping("/nao-lidas/count")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        long count = notificacaoService.contarNaoLidas(usuario.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Marca uma notificação como lida.
     */
    @PutMapping("/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable("id") Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        notificacaoService.marcarComoLida(id, usuario.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Marca todas as notificações do usuário como lidas.
     */
    @PutMapping("/lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioService.buscarPorEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        notificacaoService.marcarTodasComoLidas(usuario.getId());
        return ResponseEntity.ok().build();
    }
}
