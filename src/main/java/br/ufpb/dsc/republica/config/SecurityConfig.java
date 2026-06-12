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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // === CORS (Cross-Origin Resource Sharing) ===
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))

                // === CSRF (Cross-Site Request Forgery) ===
                .csrf(csrf -> csrf.disable()) // Desabilita para facilitar o desenvolvimento da API REST

                // === AUTORIZAÇÃO DE REQUISIÇÕES ===
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos da SPA, login, cadastro e health check são públicos
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/actuator/health",
                                "/ping",
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico"
                        ).permitAll()
                        // Qualquer outra requisição exige autenticação
                        .anyRequest().authenticated()
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
                            response.getWriter().write("{\"erro\": \"Credenciais inválidas. Verifique seu e-mail e senha.\"}");
                        })
                        .permitAll()
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

