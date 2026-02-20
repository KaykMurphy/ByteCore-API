package com.byteCore.demo.dto.mapper;

import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.request.RegisterRequestDTO;
import com.byteCore.demo.dto.response.UserResponseDTO;
import com.byteCore.demo.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {Role.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", expression = "java(Role.USER)")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "availableBalance", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "pendingBalance", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "verifications", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "products", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "withdrawals", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "pixKey", ignore = true)
    UserEntity toEntity(RegisterRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "ADMIN")
    @Mapping(target = "password", source = "encodedPassword")
    UserEntity toAdmin(String name, String email, String encodedPassword);

    UserResponseDTO toResponseDTO(UserEntity user);
}

