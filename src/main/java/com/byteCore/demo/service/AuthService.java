package com.byteCore.demo.service;

import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.UserMapper;
import com.byteCore.demo.dto.request.LoginRequestDTO;
import com.byteCore.demo.dto.request.RegisterRequestDTO;
import com.byteCore.demo.dto.response.LoginResponseDTO;
import com.byteCore.demo.dto.response.UserResponseDTO;
import com.byteCore.demo.exceptions.DuplicateEmailException;
import com.byteCore.demo.repository.UserRepository;
import com.byteCore.demo.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;


    @Transactional
    public UserResponseDTO register(RegisterRequestDTO dto){

        log.info("Registration attempt for email: {}", dto.email());

        if (userRepository.findByEmail(dto.email()).isPresent()){
            throw new DuplicateEmailException("Email already exists");
        }

        UserEntity user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));

        if (user.getAvailableBalance() == null) {
            user.setAvailableBalance(BigDecimal.ZERO);
        }

        if (user.getPendingBalance() == null) {
            user.setPendingBalance(BigDecimal.ZERO);
        }

        userRepository.save(user);

        log.info("User registered successfully: {}", user.getEmail());

        return userMapper.toResponseDTO(user);
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String roleName = user.getRole().name();
        String token = jwtUtils.generateToken(user.getEmail(), roleName);

        return new LoginResponseDTO(token);
    }
}
