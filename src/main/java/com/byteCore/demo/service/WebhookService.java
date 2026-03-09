package com.byteCore.demo.service;

import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.domain.PaymentEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.enums.OrderStatus;
import com.byteCore.demo.enums.PaymentStatus;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.PaymentRepository;
import com.byteCore.demo.repository.UserRepository;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
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
    private final DigitalProductDeliveryService deliveryService;
    private final OrderService orderService;
    private final UserRepository  userRepository;
    private final PaymentClient paymentClient;

    @Value("${mercadopago.webhook.secret}")
    private String webhookSecret;

    public boolean validateSignature(String xSignature, String xRequestId, String dataId) {
        try {
            if (xSignature == null || !xSignature.contains(",")) {
                log.warn("WEBHOOK REJEITADO: Assinatura ausente ou inválida");
                return false;
            }

            String ts = null;
            String v1 = null;
            String[] parts = xSignature.split(",");

            for (String part : parts) {
                if (part.startsWith("ts=")) ts = part.substring(3);
                else if (part.startsWith("v1=")) v1 = part.substring(3);
            }

            if (ts == null || v1 == null) {
                log.warn("WEBHOOK REJEITADO: Componentes faltando");
                return false;
            }

            // Template de validação
            String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId, xRequestId, ts);

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            boolean isValid = hexString.toString().equals(v1);

            if (!isValid) {
                log.warn("WEBHOOK REJEITADO: Assinatura inválida");
            }

            return isValid;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Erro na validação: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    public void handleNotification(String action, String type, String dataId) {
        log.info("Processando Webhook - Action: {}, Type: {}, ID: {}", action, type, dataId);

        if (!"payment".equalsIgnoreCase(type)) {
            log.debug("Ignorando tipo: {}", type);
            return;
        }

        processPaymentUpdate(dataId);
    }

    private void processPaymentUpdate(String externalId) {
        try {
            Payment mpPayment = paymentClient.get(Long.parseLong(externalId));

            PaymentEntity localPayment = paymentRepository.findByExternalId(externalId)
                    .orElseThrow(() -> new RuntimeException(
                            "Pagamento não encontrado: " + externalId
                    ));

            PaymentStatus newStatus = mapStatus(mpPayment.getStatus());

            if (localPayment.getStatus() != newStatus) {
                updatePaymentAndOrder(localPayment, newStatus);
            }

        } catch (Exception e) {
            log.error("Erro ao processar webhook: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void updatePaymentAndOrder(PaymentEntity payment, PaymentStatus newStatus) {

        log.info("Atualizando pagamento {} -> {}", payment.getExternalId(), newStatus);

        payment.setStatus(newStatus);

        if (newStatus ==  PaymentStatus.APPROVED) {
            payment.setPaidAt(Instant.now());

            OrderEntity order = payment.getOrder();

            if (order != null && !order.getItems().isEmpty()) {
                UserEntity seller = order.getItems().get(0).getProduct().getSeller();

                if (seller != null){
                    boolean hasGoodReview = seller.getAverageRating() != null &&
                            seller.getAverageRating().compareTo(new BigDecimal("4.0")) >= 0;

                    payment.calculateReleaseDate(hasGoodReview);
                    seller.addToPendingBalance(payment.getSellerAmount());
                    userRepository.save(seller);

                    log.info("Saldo pendente de R$ {} adicionado para o vendedor {}",
                            payment.getSellerAmount(), seller.getEmail());
                }else {
                    log.warn("Produto do pedido #{} não possui vendedor associado.",
                            order.getId());
                }
            }
        }

        paymentRepository.save(payment);

        OrderEntity orderForStatus = payment.getOrder();
        if (orderForStatus != null) {
            processOrderStatusChange(orderForStatus, newStatus);
        }
    }

    private void processOrderStatusChange(OrderEntity order, PaymentStatus paymentStatus) {

        OrderStatus newOrderStatus = switch (paymentStatus) {
            case APPROVED -> OrderStatus.PAID;
            case REJECTED, CANCELLED, EXPIRED -> OrderStatus.CANCELLED;
            default -> order.getStatus();
        };

        if (order.getStatus() == newOrderStatus) {
            return;
        }

        log.info("Pedido #{} atualizado para {}", order.getId(), newOrderStatus);

        // Atualizar status
        order.setStatus(newOrderStatus);

        if (newOrderStatus == OrderStatus.PAID) {
            order.setPaidAt(Instant.now());

            processApprovedOrder(order);
        }

        orderRepository.save(order);
    }

    private void processApprovedOrder(OrderEntity order) {

        log.info("Pedido #{} APROVADO - Iniciando entrega automática", order.getId());

        try {
            String toEmail = order.getDeliveryEmail();
            String userName = order.getUser().getName();
            Long orderId = order.getId();
            BigDecimal totalAmount = order.getTotalAmount();

            emailService.sendPaymentConfirmation(toEmail, userName, orderId, totalAmount);

            deliveryService.deliverOrder(order);

        } catch (Exception e) {
            log.error("Erro ao processar pedido aprovado #{}: {}",
                    order.getId(), e.getMessage(), e);
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
            default -> {
                log.warn("Status desconhecido: {}", mpStatus);
                yield PaymentStatus.PENDING;
            }
        };
    }
}