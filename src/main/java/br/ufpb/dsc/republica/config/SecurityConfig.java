package br.ufpb.dsc.republica.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

/**
 * Configuração de segurança da aplicação usando Spring Security 6.
 * Adaptado para funcionar como REST API com suporte a Cookies/Session e CORS.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2SuccessHandler customOAuth2SuccessHandler) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(List.of(
                            "http://localhost:*",
                            "http://127.0.0.1:*",
                            "http://*.dsc.rodrigor.com",
                            "https://*.dsc.rodrigor.com"
                    ));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))

                // === CSRF (Cross-Site Request Forgery) ===
                .csrf(csrf -> csrf.disable()) // Desabilita para facilitar o desenvolvimento da API REST

                // === AUTORIZAÇÃO DE REQUISIÇÕES ===
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos básicos do sistema
                        .requestMatchers("/actuator/health", "/ping").permitAll()
                        // Outros endpoints do Actuator exigem autenticação
                        .requestMatchers("/actuator/**").authenticated()
                        // Endpoints públicos da API de Autenticação
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/confirmar-email", "/api/auth/esqueceu-senha", "/api/auth/redefinir-senha", "/api/auth/validar-token-redefinicao").permitAll()
                        // Outros endpoints da API exigem autenticação
                        .requestMatchers("/api/**").authenticated()
                        // Qualquer outra requisição (páginas da SPA, CSS, JS, etc.) é pública
                        .anyRequest().permitAll()
                )

                // === FORMULÁRIO DE LOGIN REST ===
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("username") // Email do usuário
                        .passwordParameter("password") // Senha do usuário
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"mensagem\": \"Login realizado com sucesso\"}");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            String erroMsg = "Credenciais inválidas. Verifique seu e-mail e senha.";
                            
                            Throwable cause = exception;
                            if (exception instanceof org.springframework.security.authentication.InternalAuthenticationServiceException && exception.getCause() != null) {
                                cause = exception.getCause();
                            }
                            
                            if (cause instanceof org.springframework.security.authentication.DisabledException) {
                                erroMsg = cause.getMessage();
                            }
                            response.getWriter().write("{\"erro\": \"" + erroMsg + "\"}");
                        })
                        .permitAll()
                )

                // === OAUTH2 LOGIN ===
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customOAuth2SuccessHandler)
                )

                // === LOGOUT ===
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"mensagem\": \"Logout realizado com sucesso\"}");
                        })
                        .permitAll()
                )

                // === EXCEÇÕES (Retorna 401 em vez de redirecionar para /login) ===
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

