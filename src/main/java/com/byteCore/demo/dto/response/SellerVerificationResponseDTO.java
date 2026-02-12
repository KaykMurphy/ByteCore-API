package com.byteCore.demo.dto.response;

import com.byteCore.demo.enums.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record SellerVerificationResponseDTO(

        Long id,
        String fullName,
        String cpf,
        VerificationStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant submittedAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant reviewedAt,

        String rejectionReason,

        Integer documentCount,

        Integer version // numero de tentativa (1, 2, 3...)
) {

   public SellerVerificationResponseDTO{
        cpf = maskCpf(cpf);
    }

    // CPF : 123.***.***-45
    private static String maskCpf(String cpf) {
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }

}

