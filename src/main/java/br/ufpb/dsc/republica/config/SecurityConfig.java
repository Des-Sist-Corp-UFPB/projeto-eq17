package br.ufpb.dsc.republica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança da aplicação usando Spring Security 6.
 * Integrado ao CustomUserDetailsService para autenticação em banco de dados.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define o algoritmo de codificação de senhas.
     * BCrypt é usado para hashear senhas no banco.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura a cadeia de filtros de segurança HTTP.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // === AUTORIZAÇÃO DE REQUISIÇÕES ===
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos, login, cadastro e health check são públicos
                        .requestMatchers("/webjars/**", "/css/**", "/js/**", "/actuator/health", "/ping", "/login", "/cadastro").permitAll()
                        // Qualquer outra requisição exige autenticação
                        .anyRequest().authenticated()
                )

                // === FORMULÁRIO DE LOGIN ===
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username") // Mapeia o campo name="username" do form (será o email)
                        .passwordParameter("password") // Mapeia o campo name="password" do form
                        .defaultSuccessUrl("/", true) // Redireciona para a home principal
                        .permitAll()
                )

                // === LOGOUT ===
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                // === CSRF (Cross-Site Request Forgery) ===
                .csrf(csrf -> csrf
                        // Desabilita CSRF para os endpoints do HTMX e cadastros para simplificar
                        .ignoringRequestMatchers("/casas/**", "/despesas/**", "/tarefas/**", "/cadastro/**")
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
