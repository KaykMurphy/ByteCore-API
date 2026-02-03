package com.byteCore.demo.dto.request;

import com.byteCore.demo.validation.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password

){}

