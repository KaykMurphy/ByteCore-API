package com.byteCore.demo.dto.response;

import java.math.BigDecimal;

public record ProductResponseDTO(

        Long id,
        String title,
        String description,
        BigDecimal price,
        String imageUrl,
        Long availableStock

) {}

