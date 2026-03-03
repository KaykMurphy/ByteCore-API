package com.byteCore.demo.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponseDTO(
    Long id,
    String status,
    BigDecimal totalAmount,
    String deliveryEmail,
    Instant createdAt,
    List<OrderItemResponseDTO> items


) { }
