package com.byteCore.demo.validation;

import com.byteCore.demo.domain.User;
import com.byteCore.demo.dto.request.RegisterRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator
        implements ConstraintValidator<PasswordMatch, RegisterRequestDTO> {

    @Override
    public boolean isValid(RegisterRequestDTO dto,
                           ConstraintValidatorContext context) {

        if (dto.password() == null || dto.confirmPassword() == null) {
            return false;
        }

        return dto.password().equals(dto.confirmPassword());
    }
}