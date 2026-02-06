package com.byteCore.demo.service;

import com.byteCore.demo.domain.Order;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${bytemarket.email.from}")
    private String senderEmail;

    @Async // Executa numa thread separada para não travar o Webhook
    public void sendPaymentConfirmation(Order order) {
        try {
            log.info("Enviando e-mail de confirmação para: {}", order.getUser().getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(senderEmail);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Pagamento Confirmado - Pedido #" + order.getId());

            String content = String.format("""
                <html>
                <body>
                    <h2>Olá, %s!</h2>
                    <p>Temos ótimas notícias! O seu pagamento para o pedido <strong>#%d</strong> foi confirmado.</p>
                    <p><strong>Valor:</strong> R$ %.2f</p>
                    <hr/>
                    <p>Seus produtos já estão sendo preparados.</p>
                    <p>Atenciosamente,<br/>Equipe ByteCore</p>
                </body>
                </html>
                """,
                    order.getUser().getName(),
                    order.getId(),
                    order.getTotalAmount()
            );

            helper.setText(content, true);

            mailSender.send(message);
            log.info("E-mail enviado com sucesso para o pedido #{}", order.getId());

        } catch (MessagingException e) {
            log.error("Falha ao enviar e-mail para o pedido #{}: {}", order.getId(), e.getMessage());
        }
    }
}