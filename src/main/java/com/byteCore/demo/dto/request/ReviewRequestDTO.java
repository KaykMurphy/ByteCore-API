package com.byteCore.demo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReviewRequestDTO(
        @NotNull
        Long orderId,

        @NotNull
        @Min(1) @Max(5)
        Integer rating,

        @Size(max = 500)
        String comment
) {
}
