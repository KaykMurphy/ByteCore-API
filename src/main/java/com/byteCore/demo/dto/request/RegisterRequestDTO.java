package com.byteCore.demo.dto.request;

import com.byteCore.demo.validation.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatch
public record RegisterRequestDTO(


        @NotBlank(message = "Name must not be blank")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        String name,

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 6, max=50)
        String password,

        @NotBlank
        String confirmPassword
)
 { }

