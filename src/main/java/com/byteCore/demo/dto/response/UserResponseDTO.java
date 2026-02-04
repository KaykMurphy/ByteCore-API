package com.byteCore.demo.dto.response;

import com.byteCore.demo.enums.Role;

import java.util.UUID;

public record UserResponseDTO(
        String name,
        String email,
        Role role
) {
}
