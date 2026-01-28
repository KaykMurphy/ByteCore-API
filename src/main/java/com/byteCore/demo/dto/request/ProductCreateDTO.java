package com.byteCore.demo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
        String imageUlr

) { }

