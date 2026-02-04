package com.byteCore.demo.service;

import com.byteCore.demo.domain.User;
import com.byteCore.demo.dto.mapper.UserMapper;
import com.byteCore.demo.dto.request.RegisterRequestDTO;
import com.byteCore.demo.dto.response.UserResponseDTO;
import com.byteCore.demo.enums.Role;
import com.byteCore.demo.exceptions.DuplicateEmailException;
import com.byteCore.demo.repository.UserRepository;
import com.byteCore.demo.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    @Transactional
    public UserResponseDTO register(RegisterRequestDTO dto){

        if (userRepository.findByEmail(dto.email()).isPresent()){
            throw new DuplicateEmailException("Email already exists");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));


        userRepository.save(user);

        return userMapper.toResponseDTO(user);
    }
}
