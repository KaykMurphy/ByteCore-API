package com.byteCore.demo.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record WithdrawalRequestDTO(


        @NotNull
        @DecimalMin("0.01")
        @DecimalMax("500.00")
        BigDecimal amount,

        @NotBlank
        @Size(max = 100)
        String pixKey
) {
}
