package br.ufpb.dsc.republica.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envia um e-mail em formato HTML para confirmação de cadastro.
     */
    public void enviarEmailConfirmacao(String emailUsuario, String nomeUsuario, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailUsuario);
            helper.setSubject("HomeHub — Confirme seu e-mail");

            String linkConfirmacao = String.format("%s/api/auth/verificar-email?token=%s", appUrl, token);

            String htmlBody = String.format(
                "<html>" +
                "<body style=\"font-family: Arial, sans-serif; color: #333333; line-height: 1.6; margin: 0; padding: 20px;\">" +
                "  <div style=\"max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; padding: 24px;\">" +
                "    <h2 style=\"color: #0f172a;\">Bem-vindo ao HomeHub, %s!</h2>" +
                "    <p>Estamos muito felizes em ter você conosco. Para começar a gerenciar suas repúblicas, por favor confirme seu e-mail clicando no botão abaixo:</p>" +
                "    <div style=\"margin: 30px 0; text-align: center;\">" +
                "      <a href=\"%s\" style=\"background-color: #00f2fe; color: #0f172a; text-decoration: none; padding: 12px 24px; font-weight: bold; border-radius: 6px; display: inline-block; box-shadow: 0 4px 6px -1px rgba(0, 242, 254, 0.2);\">" +
                "        Confirmar E-mail" +
                "      </a>" +
                "    </div>" +
                "    <p style=\"font-size: 0.9rem; color: #64748b;\">Se o botão acima não funcionar, copie e cole o link a seguir no seu navegador:</p>" +
                "    <p style=\"font-size: 0.85rem; color: #00f2fe; word-break: break-all;\">%s</p>" +
                "    <hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;\">" +
                "    <p style=\"font-size: 0.8rem; color: #94a3b8; margin: 0;\">Este link é válido por 24 horas. Se você não realizou este cadastro, pode ignorar esta mensagem.</p>" +
                "  </div>" +
                "</body>" +
                "</html>",
                nomeUsuario,
                linkConfirmacao,
                linkConfirmacao
            );

            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("E-mail de confirmação enviado com sucesso para: {}", emailUsuario);

        } catch (Exception e) {
            log.error("FALHA AO ENVIAR E-MAIL de confirmação para: {}. Motivo: {}", emailUsuario, e.getMessage(), e);
            log.warn("=== LINK DE VERIFICAÇÃO (Para uso caso o envio falhe): {}/api/auth/verificar-email?token={} ===", appUrl, token);
        }
    }
}
