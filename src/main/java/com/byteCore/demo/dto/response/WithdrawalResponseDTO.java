package com.byteCore.demo.dto.response;

import com.byteCore.demo.enums.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawalResponseDTO(
        UUID id,
        BigDecimal amount,
        String pixKey,
        WithdrawalStatus status,
        Instant requestedAt,
        Instant processedAt,
        String adminNotes,
        String transactionId
) {
}
