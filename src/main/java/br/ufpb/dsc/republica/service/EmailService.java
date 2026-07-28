package br.ufpb.dsc.republica.service;

import br.ufpb.dsc.republica.domain.Usuario;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.api.trace.Span;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.url:https://eq17.dsc.rodrigor.com}")
    private String appUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @WithSpan("enviar-email-confirmacao")
    public void enviarEmailConfirmacao(Usuario usuario, String token) {
        Span.current().setAttribute("email.destinatario", usuario.getEmail());
        Span.current().setAttribute("usuario.nome", usuario.getNome());
        if (fromEmail == null || fromEmail.isEmpty()) {
            logger.warn("E-mail de remetente (spring.mail.username) não configurado. O e-mail de confirmação não foi enviado.");
            logger.info("Link de confirmação simulado: {}/api/auth/confirmar-email?token={}", appUrl, token);
            return;
        }

        String urlConfirmacao = appUrl + "/api/auth/confirmar-email?token=" + token;
        String htmlBody = String.format(
                "<h3>Olá, %s!</h3>" +
                "<p>Obrigado por se cadastrar no HomeHub. Para ativar sua conta, clique no link abaixo:</p>" +
                "<p><a href='%s' style='display:inline-block;padding:10px 20px;color:#fff;background-color:#007bff;text-decoration:none;border-radius:5px;'>Confirmar E-mail</a></p>" +
                "<p>Se o botão acima não funcionar, copie e cole o link a seguir no seu navegador:</p>" +
                "<p>%s</p>" +
                "<br><p>Atenciosamente,<br>Equipe HomeHub</p>",
                usuario.getNome(), urlConfirmacao, urlConfirmacao
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(usuario.getEmail());
            helper.setSubject("Confirmação de E-mail — HomeHub");
            helper.setText(htmlBody, true); // true indica HTML

            mailSender.send(message);
            logger.info("E-mail de confirmação enviado com sucesso para: {}", usuario.getEmail());
        } catch (Exception e) {
            logger.error("Falha ao enviar e-mail de confirmação para {}: {}", usuario.getEmail(), e.getMessage());
            // Não relança a exceção para não invalidar a transação de cadastro do usuário
        }
    }
}
