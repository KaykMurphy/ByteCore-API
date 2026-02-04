package com.byteCore.demo.dto.mapper;

import com.byteCore.demo.domain.User;
import com.byteCore.demo.dto.request.RegisterRequestDTO;
import com.byteCore.demo.dto.response.UserResponseDTO;
import com.byteCore.demo.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {Role.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "password", ignore = true)
    User toEntity(RegisterRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "ADMIN")
    @Mapping(target = "password", source = "encodedPassword")
    User toAdmin(String name, String email, String encodedPassword);

    UserResponseDTO toResponseDTO(User user);
}
