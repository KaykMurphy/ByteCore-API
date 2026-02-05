package com.byteCore.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Resposta enviada ao frontend após criação do pagamento PIX
 */

public record PixPaymentResponseDTO(

        Long paymentId, // ID interno do sistema
        String externalId, // ID do Mercado Pago
        BigDecimal amount,
        String status,

        String pixQrCode,
        String pixQrCodeText,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant expiresAt,

        Long orderId

) {
}
