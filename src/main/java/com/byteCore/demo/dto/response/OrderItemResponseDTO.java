package com.byteCore.demo.dto.response;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
        Long productId,
        String productTitle,
        Integer quantity,
        BigDecimal price,
        BigDecimal subtotal,
        boolean delivered
) {
}
