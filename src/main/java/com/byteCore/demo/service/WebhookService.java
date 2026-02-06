package com.byteCore.demo.service;

import com.byteCore.demo.domain.Order;
import com.byteCore.demo.domain.PaymentEntity;
import com.byteCore.demo.enums.OrderStatus;
import com.byteCore.demo.enums.PaymentStatus;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.PaymentRepository;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Value("${mercadopago.webhook.secret}")
    private String webhookSecret;

    public boolean validateSignature(String xSignature, String xRequestId, String dataId) {
        try {
            if (xSignature == null || !xSignature.contains(",")) {
                return true;
            }

            String ts = null;
            String v1 = null;
            String[] parts = xSignature.split(",");

            for (String part : parts) {
                if (part.startsWith("ts=")) ts = part.substring(3);
                else if (part.startsWith("v1=")) v1 = part.substring(3);
            }

            if (ts == null || v1 == null) return false;

            // Template: id:[data.id];request-id:[x-request-id];ts:[ts];
            String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId, xRequestId, ts);

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(v1);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Erro na validação criptográfica: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    public void handleNotification(String action, String type, String dataId) {
        log.info("Processando Webhook - Action: {}, Type: {}, ID: {}", action, type, dataId);

        if (!"payment".equalsIgnoreCase(type)) {
            log.debug("Ignorando tópico irrelevante: {}", type);
            return;
        }

        processPaymentUpdate(dataId);
    }

    private void processPaymentUpdate(String externalId) {
        try {
            PaymentClient client = new PaymentClient();
            Payment mpPayment = client.get(Long.parseLong(externalId));

            PaymentEntity localPayment = paymentRepository.findByExternalId(externalId)
                    .orElseThrow(() -> new RuntimeException("Pagamento local não encontrado: " + externalId));

            PaymentStatus newStatus = mapStatus(mpPayment.getStatus());

            // Idempotência: só processa se houver mudança de status
            if (localPayment.getStatus() != newStatus) {
                updateLocalRecords(localPayment, newStatus);
            }

        } catch (Exception e) {
            log.error("Falha ao sincronizar pagamento {}: {}", externalId, e.getMessage());
            throw new RuntimeException("Erro no processamento do webhook", e);
        }
    }

    private void updateLocalRecords(PaymentEntity payment, PaymentStatus newStatus) {
        log.info("Atualizando pagamento {} -> {}", payment.getExternalId(), newStatus);

        payment.setStatus(newStatus);
        if (newStatus == PaymentStatus.APPROVED) {
            payment.setPaidAt(Instant.now());
        }
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            updateOrderStatus(order, newStatus);
        }
    }

    private void updateOrderStatus(Order order, PaymentStatus paymentStatus) {
        OrderStatus newOrderStatus = switch (paymentStatus) {
            case APPROVED -> OrderStatus.PAID;
            case REJECTED, CANCELLED, EXPIRED -> OrderStatus.CANCELLED;
            default -> order.getStatus();
        };

        if (order.getStatus() != newOrderStatus) {
            log.info("Pedido #{} atualizado para {}", order.getId(), newOrderStatus);
            order.setStatus(newOrderStatus);
            orderRepository.save(order);

            if (newOrderStatus == OrderStatus.PAID) {
                emailService.sendPaymentConfirmation(order);
            }
        }
    }

    private PaymentStatus mapStatus(String mpStatus) {
        if (mpStatus == null) return PaymentStatus.PENDING;
        return switch (mpStatus.toLowerCase()) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "expired" -> PaymentStatus.EXPIRED;
            case "in_process", "pending" -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING;
        };
    }
}