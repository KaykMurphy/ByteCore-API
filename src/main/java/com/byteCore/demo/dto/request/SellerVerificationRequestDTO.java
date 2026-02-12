package com.byteCore.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SellerVerificationRequestDTO (

        @NotBlank(message = "Nome completo é obrigatório")
        @Size(min = 3, max = 200)
        String fullName,

        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
        String cpf,

        @NotBlank(message = "Telefone é obrigatório")
        @Size(min = 10, max = 15)
        String phone,

        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos")
        String cep,

        @NotBlank
        @Size(max = 500)
        String address,

        @Size(max = 1000)
        String additionalInfo


){

}

