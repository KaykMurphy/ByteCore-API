package com.byteCore.demo.controller;

import com.byteCore.demo.domain.User;
import com.byteCore.demo.dto.mapper.UserMapper;
import com.byteCore.demo.dto.request.LoginRequestDTO;
import com.byteCore.demo.dto.request.RegisterRequestDTO;
import com.byteCore.demo.dto.response.LoginResponseDTO;
import com.byteCore.demo.dto.response.UserResponseDTO;
import com.byteCore.demo.security.CustomUserDetails;
import com.byteCore.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register (
            @Valid @RequestBody RegisterRequestDTO dto) {

        log.info("POST /auth/register - Email: {}", dto.email());

        UserResponseDTO response = authService.register(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login (
            @Valid @RequestBody LoginRequestDTO dto) {

        log.info("POST /auth/login - Email: {}", dto.email());

        LoginResponseDTO response = authService.login(dto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        log.info("GET /auth/me");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user =  userDetails.getUser();

        UserResponseDTO response = userMapper.toResponseDTO(user);

        return ResponseEntity.ok(response);
    }
}
