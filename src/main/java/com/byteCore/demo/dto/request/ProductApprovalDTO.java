package com.byteCore.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductApprovalDTO(

        @NotNull(message = "Decisão é obrigatória")
        Boolean approved,

        @Size(max = 1500, message = "Motivo muito longo")
        String reason

) {
}

