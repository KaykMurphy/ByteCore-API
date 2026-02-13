package com.byteCore.demo.dto.request;

import com.byteCore.demo.enums.ProductType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductCreateDTO (

        @NotBlank
        @Size(min = 3, max = 100)
        String title,

        @NotBlank
        @Size(min = 3, max = 5000)
        String description,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal price,

        @NotBlank
        String imageUrl,

        @NotNull(message = "Tipo do produto é obrigatório")
        ProductType type,

        @Size(max = 100)
        String platform, // steam, epic ...

        @Size(max = 50)
        String region, // global, br, na ...

        @Min(0)
        Long availableStock,

        @Size(max = 2000)
        String activationInstructions,

        @Size(max = 1000)
        String importantNotes,

        @Min(1)
        Integer estimatedDeliveryMinutes
) { }

