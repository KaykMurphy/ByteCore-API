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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${bytemarket.email.from}")
    private String senderEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendPaymentConfirmation(Order order) {
        try {
            log.info("Enviando confirmação de pagamento para: {}", order.getDeliveryEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(senderEmail);
            helper.setTo(order.getDeliveryEmail());
            helper.setSubject("Pagamento Confirmado - Pedido #" + order.getId());

            String content = buildPaymentConfirmationEmail(order);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email de confirmação enviado com sucesso");

        } catch (MessagingException e) {
            log.error("Erro ao enviar email de confirmação: {}", e.getMessage());
        }
    }

    @Async
    public void sendDigitalProductsEmail(
            String email,
            Long orderId,
            List<String> products) {

        try {
            log.info("Enviando produtos digitais para: {}", email);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(senderEmail);
            helper.setTo(email);
            helper.setSubject("Seus Produtos Digitais - Pedido #" + orderId);

            String content = buildDigitalProductsEmail(orderId, products);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email com produtos digitais enviado com sucesso");

        } catch (MessagingException e) {
            log.error("Erro ao enviar produtos digitais: {}", e.getMessage());
        }
    }

    private String buildPaymentConfirmationEmail(Order order) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;">
                    
                    <h1 style="color: #4CAF50; text-align: center;">✅ Pagamento Confirmado!</h1>
                    
                    <p>Olá <strong>%s</strong>,</p>
                    
                    <p>Ótimas notícias! Seu pagamento PIX foi confirmado com sucesso.</p>
                    
                    <div style="background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0;">Detalhes do Pedido</h3>
                        <p><strong>Pedido:</strong> #%d</p>
                        <p><strong>Valor:</strong> R$ %.2f</p>
                        <p><strong>Status:</strong> Pago ✅</p>
                    </div>
                    
                    <p>🚀 Seus produtos digitais estão sendo processados e serão enviados em breve para este email!</p>
                    
                    <p style="margin-top: 40px; color: #666; font-size: 12px;">
                        Se você tiver alguma dúvida, responda este email.<br/>
                        Equipe ByteCore
                    </p>
                </div>
            </body>
            </html>
            """,
                order.getUser().getName(),
                order.getId(),
                order.getTotal()
        );
    }

    private String buildDigitalProductsEmail(Long orderId, List<String> products) {

        StringBuilder productsHtml = new StringBuilder();

        for (String product : products) {
            String htmlProduct = product.replace("\n", "<br/>");

            productsHtml.append(
                    String.format("""
                    <div style="background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin: 15px 0; border-left: 4px solid #4CAF50;">
                        <pre style="font-family: 'Courier New', monospace; margin: 0; white-space: pre-wrap;">%s</pre>
                    </div>
                    """, htmlProduct)
            );
        }

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 700px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;">
                    
                    <h1 style="color: #4CAF50; text-align: center;">🎁 Seus Produtos Digitais</h1>
                    
                    <p>Seu pedido <strong>#%d</strong> foi processado com sucesso!</p>
                    
                    <p>Abaixo estão seus produtos digitais. <strong>Guarde este email em local seguro!</strong></p>
                    
                    <hr style="border: 1px solid #eee; margin: 30px 0;"/>
                    
                    %s
                    
                    <hr style="border: 1px solid #eee; margin: 30px 0;"/>
                    
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; border-left: 4px solid #ffc107;">
                        <strong>⚠️ IMPORTANTE:</strong>
                        <ul style="margin: 10px 0;">
                            <li>Guarde suas keys/contas em local seguro</li>
                            <li>Não compartilhe com terceiros</li>
                            <li>Em caso de problemas, entre em contato em até 24h</li>
                        </ul>
                    </div>
                    
                    <p style="text-align: center; margin-top: 40px;">
                        <a href="%s/meus-pedidos" style="background-color: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            Ver Meus Pedidos
                        </a>
                    </p>
                    
                    <p style="margin-top: 40px; color: #666; font-size: 12px; text-align: center;">
                        Obrigado por comprar com a ByteCore!<br/>
                        Se tiver dúvidas, responda este email.
                    </p>
                </div>
            </body>
            </html>
            """,
                orderId,
                productsHtml.toString(),
                baseUrl
        );
    }
}