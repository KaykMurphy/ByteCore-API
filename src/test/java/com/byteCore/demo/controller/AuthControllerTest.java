package com.byteCore.demo.controller;

import com.byteCore.demo.config.SecurityConfig;
import com.byteCore.demo.dto.mapper.UserMapper;
import com.byteCore.demo.dto.request.RegisterRequestDTO;
import com.byteCore.demo.dto.response.UserResponseDTO;
import com.byteCore.demo.enums.Role;
import com.byteCore.demo.security.JwtAuthenticationFilter;
import com.byteCore.demo.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        JwtAuthenticationFilter.class,
                        SecurityConfig.class
                })
        }
)

@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserMapper userMapper;

    @Test
    @DisplayName("POST /auth/register - Should return 201 Created when data is valid")
    void register_shouldReturn201_whenDataIsValid() throws Exception {

        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "Arthur Morgan",
                "arthur@vanderlinde.com",
                "senha123",
                "senha123"
        );

        UserResponseDTO responseDTO = new UserResponseDTO(
                "Arthur Morgan",
                "arthur@vanderlinde.com",
                Role.USER
        );

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenReturn(responseDTO);


        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Arthur Morgan"))
                .andExpect(jsonPath("$.email").value("arthur@vanderlinde.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /auth/register - Should return 400 Bad Request when email is invalid")
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {

        RegisterRequestDTO invalidRequest = new RegisterRequestDTO(
                "",
                "email-invalido-sem-arroba",
                "senha123",
                "senha123"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());

    }


}
