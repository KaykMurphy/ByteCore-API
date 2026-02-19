package com.byteCore.demo.service;

import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.domain.PaymentEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.PaymentMapper;
import com.byteCore.demo.dto.response.PixPaymentResponseDTO;
import com.byteCore.demo.enums.PaymentMethod;
import com.byteCore.demo.enums.PaymentStatus;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.PaymentRepository;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class PixPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    @Value("${app.base-url}")
    private String baseUrl;


    @Transactional
    public PixPaymentResponseDTO createPixPayment(UserEntity user, BigDecimal amount, Long orderId) {
        try {
            // Buscamos o pedido para evitar o erro de ORDER_ID nulo no banco
            OrderEntity order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

            PaymentClient paymentClient = new PaymentClient();

            // payer
            PaymentPayerRequest payer = PaymentPayerRequest.builder()
                    .email(user.getEmail())
                    .firstName(user.getName())
                    .build();

            // cria pix
            PaymentCreateRequest request = PaymentCreateRequest.builder()
                    .transactionAmount(amount)
                    .paymentMethodId("pix")
                    .description("Pedido #" + orderId)
                    .payer(payer)
                    .externalReference(orderId.toString())
                    .notificationUrl(baseUrl + "/api/webhooks/mercadopago")
                    .dateOfExpiration(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(30))
                    .build();

            // request > response > json > objeto
            Payment mpPayment = paymentClient.create(request);

            /// filtra dados mp
            PaymentEntity localPayment = new PaymentEntity();
            localPayment.setExternalId(mpPayment.getId().toString());
            localPayment.setCreatedAt(mpPayment.getDateCreated().toInstant());
            localPayment.setAmount(mpPayment.getTransactionAmount());
            localPayment.setStatus(PaymentStatus.PENDING);
            localPayment.setMethod(PaymentMethod.PIX);
            localPayment.setUser(user);
            localPayment.setOrder(order); // Vinculamos o pedido ao pagamento
            localPayment.setExpiresAt(mpPayment.getDateOfExpiration().toInstant());

            String qrCodeBase64 = mpPayment.getPointOfInteraction().getTransactionData().getQrCodeBase64();
            String qrCodeText = mpPayment.getPointOfInteraction().getTransactionData().getQrCode();
            localPayment.setQrCode(qrCodeText);

            paymentRepository.save(localPayment);

            return paymentMapper.toDto(localPayment, qrCodeBase64, qrCodeText, orderId);

        } catch (MPApiException e) {
            log.error("Erro na API do Mercado Pago: {}", e.getApiResponse().getContent());
            throw new RuntimeException("Erro ao criar pagamento no provedor");
        } catch (MPException e) {
            log.error("Erro de conexão com Mercado Pago", e);
            throw new RuntimeException("Erro interno de pagamento");
        }
    }

    // ver status
    @Transactional
    public PixPaymentResponseDTO getPaymentStatus(Long paymentId) {
        PaymentEntity localPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));

        if (localPayment.getStatus() == PaymentStatus.PENDING) {
            syncStatusWithMercadoPago(localPayment); // sync mp
        }

        return paymentMapper.toDto(localPayment, null, localPayment.getQrCode(), null);
    }

    // sync mp
    private void syncStatusWithMercadoPago(PaymentEntity localPayment) {
        try {
            log.info("Sincronizando status para o pagamento local ID: {}", localPayment.getId());

            PaymentClient client = new PaymentClient();
            Payment mpPayment = client.get(Long.parseLong(localPayment.getExternalId()));

            PaymentStatus currentMpStatus = mapStatus(mpPayment.getStatus());

            if (currentMpStatus != localPayment.getStatus()) {
                localPayment.setStatus(currentMpStatus);

                if (currentMpStatus == PaymentStatus.APPROVED) {
                    localPayment.setPaidAt(Instant.now());
                }

                paymentRepository.save(localPayment);
            }
        } catch (Exception e) {
            log.warn("Não foi possível sincronizar o pagamento {}: {}", localPayment.getExternalId(), e.getMessage());
        }
    }

    // map status
    private PaymentStatus mapStatus(String mpStatus) {
        if (mpStatus == null) {
            return PaymentStatus.PENDING;
        }
        return switch (mpStatus) {
            case "approved" -> PaymentStatus.APPROVED;
            case "pending", "in_process" -> PaymentStatus.PENDING;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "refunded", "charged_back" -> PaymentStatus.CANCELLED;
            default -> {
                log.warn("Status desconhecido recebido do Mercado Pago: {}", mpStatus);
                yield PaymentStatus.PENDING;
            }
        };
    }
}