package com.byteCore.demo.controller;

import com.byteCore.demo.config.SecurityConfig;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.UserMapper;
import com.byteCore.demo.dto.request.LoginRequestDTO;
import com.byteCore.demo.dto.request.RegisterRequestDTO;
import com.byteCore.demo.dto.response.LoginResponseDTO;
import com.byteCore.demo.dto.response.UserResponseDTO;
import com.byteCore.demo.enums.Role;
import com.byteCore.demo.security.CustomUserDetails;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @DisplayName("POST /auth/login - Should return 200 OK when data is valid")
    void login_shouldReturn200_whenDataIsValid() throws Exception{

        LoginRequestDTO validLogin = new LoginRequestDTO(
                "emailteste@gmail.com",
                "senha123"
        );

        LoginResponseDTO response = new LoginResponseDTO(
                "token-ultra-secreto"
        );

        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLogin)))

                .andExpect(status().isOk())


                .andExpect(jsonPath("$.token").value("token-ultra-secreto"));    }

    @Test
    @DisplayName("POST /auth/login - Should return 400 Bad Request when email is invalid")
    void login_shouldReturn400_whenEmailIsInvalid() throws Exception{

        LoginRequestDTO invalidEmail = new LoginRequestDTO(
                "email-sem-arroba",
                "senha1234"
        );

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmail)))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("GET /auth/me - Should return 200 OK and user data when authenticated")
    void getCurrentUser_shouldReturn200_whenAuthenticated() throws Exception {

        UserEntity mockUser = new UserEntity();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("John Marston");
        mockUser.setEmail("john@rdr.com");
        mockUser.setRole(Role.USER);

        CustomUserDetails customUserDetails = new CustomUserDetails(mockUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);

        SecurityContextHolder.setContext(securityContext);

        UserResponseDTO responseDTO = new UserResponseDTO(
                mockUser.getName(),
                mockUser.getEmail(),
                mockUser.getRole()
        );

        when(userMapper.toResponseDTO(any(UserEntity.class))).thenReturn(responseDTO);

        try {
            mockMvc.perform(get("/auth/me")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("John Marston"))
                    .andExpect(jsonPath("$.email").value("john@rdr.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
