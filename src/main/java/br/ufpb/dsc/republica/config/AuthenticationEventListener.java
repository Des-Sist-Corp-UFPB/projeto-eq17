package br.ufpb.dsc.republica.config;

import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.service.AuditoriaService;
import br.ufpb.dsc.republica.service.UsuarioService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationEventListener {

    private final AuditoriaService auditoriaService;
    private final UsuarioService usuarioService;

    public AuthenticationEventListener(AuditoriaService auditoriaService, UsuarioService usuarioService) {
        this.auditoriaService = auditoriaService;
        this.usuarioService = usuarioService;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        String email = "";
        if (principal instanceof User) {
            email = ((User) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        }

        if (!email.isEmpty()) {
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                auditoriaService.registrar(usuario, "LOGIN", "Login realizado com sucesso no sistema.", "Usuario", usuario.getId());
            }
        }
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String email = event.getAuthentication().getName();
        auditoriaService.registrar(null, "LOGIN_FALHA", "Tentativa de login malsucedida para o e-mail: " + email, "Usuario", null);
    }
}
