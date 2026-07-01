package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient;

    @Value("${resend.api.key:}")
    private String apiKey;

    @Value("${resend.from:onboarding@resend.dev}")
    private String fromEmail;

    public EmailService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .build();
    }

    public void enviarEmailConfirmacao(Usuario usuario, String token) {
        if (apiKey == null || apiKey.isEmpty() || "dummy-key".equals(apiKey)) {
            logger.warn("Chave de API do Resend não configurada. O e-mail de confirmação não foi enviado.");
            logger.info("Link de confirmação simulado: http://localhost:8080/api/auth/confirmar-email?token={}", token);
            return;
        }

        String urlConfirmacao = "http://localhost:8080/api/auth/confirmar-email?token=" + token;
        String htmlBody = String.format(
                "<h3>Olá, %s!</h3>" +
                "<p>Obrigado por se cadastrar no HomeHub. Para ativar sua conta, clique no link abaixo:</p>" +
                "<p><a href='%s' style='display:inline-block;padding:10px 20px;color:#fff;background-color:#007bff;text-decoration:none;border-radius:5px;'>Confirmar E-mail</a></p>" +
                "<p>Se o botão acima não funcionar, copie e cole o link a seguir no seu navegador:</p>" +
                "<p>%s</p>" +
                "<br><p>Atenciosamente,<br>Equipe HomeHub</p>",
                usuario.getNome(), urlConfirmacao, urlConfirmacao
        );

        Map<String, Object> requestBody = Map.of(
                "from", fromEmail,
                "to", List.of(usuario.getEmail()),
                "subject", "Confirmação de E-mail — HomeHub",
                "html", htmlBody
        );

        try {
            restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            logger.info("E-mail de confirmação enviado com sucesso para: {}", usuario.getEmail());
        } catch (Exception e) {
            logger.error("Falha ao enviar e-mail de confirmação para {}: {}", usuario.getEmail(), e.getMessage());
            // Não relança a exceção para não invalidar a transação de cadastro do usuário
        }
    }
}
