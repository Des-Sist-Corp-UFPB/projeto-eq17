package br.ufpb.dsc.republica.config;

import br.ufpb.dsc.republica.domain.Usuario;
import br.ufpb.dsc.republica.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioService usuarioService;

    public CustomOAuth2SuccessHandler(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String nome = oAuth2User.getAttribute("name");

        if (email == null) {
            response.sendRedirect("/login?error=email_not_provided");
            return;
        }

        usuarioService.registrarOuObterUsuarioOAuth2(email, nome);

        // Redirect back to SPA root
        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}
