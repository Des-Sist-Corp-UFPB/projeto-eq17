package br.ufpb.dsc.republica.controller;

import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.dto.UsuarioDto;
import br.ufpb.dsc.republica.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * Valida o token de confirmação e ativa o cadastro do usuário.
     */
    @GetMapping("/verificar-email")
    public ResponseEntity<String> verificarEmail(@RequestParam("token") String token) {
        try {
            usuarioService.verificarEmail(token);
            String htmlSucesso = "<html>" +
                    "<head>" +
                    "  <meta charset=\"UTF-8\">" +
                    "  <title>E-mail Confirmado | HomeHub</title>" +
                    "</head>" +
                    "<body style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0b1329; color: #f8fafc; text-align: center; padding: 100px 20px; margin: 0;\">" +
                    "  <div style=\"max-width: 500px; margin: 0 auto; padding: 40px; border: 1px solid #1e293b; border-radius: 12px; background-color: #0f172a; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);\">" +
                    "    <h2 style=\"color: #10b981; font-weight: 800; font-size: 1.8rem; margin-bottom: 16px;\">E-mail verificado com sucesso!</h2>" +
                    "    <p style=\"color: #94a3b8; font-size: 1rem; line-height: 1.6;\">Sua conta está ativada. Você já pode abrir o HomeHub e realizar o login.</p>" +
                    "    <a href=\"/\" style=\"display: inline-block; background-color: #00f2fe; color: #0f172a; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; margin-top: 24px; box-shadow: 0 4px 10px rgba(0, 242, 254, 0.2); transition: background-color 0.2s;\">" +
                    "      Ir para o HomeHub" +
                    "    </a>" +
                    "  </div>" +
                    "</body>" +
                    "</html>";
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html;charset=UTF-8")
                    .body(htmlSucesso);
        } catch (Exception e) {
            String htmlErro = "<html>" +
                    "<head>" +
                    "  <meta charset=\"UTF-8\">" +
                    "  <title>Falha na Verificação | HomeHub</title>" +
                    "</head>" +
                    "<body style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0b1329; color: #f8fafc; text-align: center; padding: 100px 20px; margin: 0;\">" +
                    "  <div style=\"max-width: 500px; margin: 0 auto; padding: 40px; border: 1px solid #1e293b; border-radius: 12px; background-color: #0f172a; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);\">" +
                    "    <h2 style=\"color: #ef4444; font-weight: 800; font-size: 1.8rem; margin-bottom: 16px;\">Falha na verificação</h2>" +
                    "    <p style=\"color: #94a3b8; font-size: 1rem; line-height: 1.6;\">" + e.getMessage() + "</p>" +
                    "    <a href=\"/\" style=\"display: inline-block; background-color: #64748b; color: #f8fafc; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; margin-top: 24px;\">" +
                    "      Voltar ao HomeHub" +
                    "    </a>" +
                    "  </div>" +
                    "</body>" +
                    "</html>";
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html;charset=UTF-8")
                    .body(htmlErro);
        }
    }
}

