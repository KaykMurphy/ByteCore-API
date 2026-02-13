package com.byteCore.demo.dto.request;

import com.byteCore.demo.enums.DeliveryType;
import com.byteCore.demo.enums.ProductType;

import java.math.BigDecimal;

public record ProductUpdateDTO (

        String title,
        String description,
        String imageUrl,
        Boolean active,
        ProductType type,
        BigDecimal price,
        String platform,
        String region,
        Long availableStock,
        DeliveryType deliveryType,
        String activationInstructions,
        String importantNotes,
        Integer estimatedDeliveryMinutes
)
{}

