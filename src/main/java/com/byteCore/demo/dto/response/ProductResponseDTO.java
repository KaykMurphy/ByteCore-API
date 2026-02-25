package com.byteCore.demo.dto.response;

import com.byteCore.demo.enums.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponseDTO(

        Long id,
        String title,
        String description,
        BigDecimal price,
        String imageUrl,
        Long availableStock,
        ProductType type,
        UUID sellerId,
        String sellerName

) {}

