package com.byteCore.demo.controller;

import com.byteCore.demo.config.SecurityConfig;
import com.byteCore.demo.domain.SellerVerificationEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.SellerVerificationMapper;
import com.byteCore.demo.dto.request.SellerVerificationRequestDTO;
import com.byteCore.demo.dto.response.SellerVerificationResponseDTO;
import com.byteCore.demo.enums.Role;
import com.byteCore.demo.enums.VerificationStatus;
import com.byteCore.demo.repository.SellerVerificationRepository;
import com.byteCore.demo.repository.UserRepository;
import com.byteCore.demo.security.CustomUserDetails;
import com.byteCore.demo.security.JwtAuthenticationFilter;
import com.byteCore.demo.service.SellerVerificationService;
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
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = SellerVerificationController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        JwtAuthenticationFilter.class,
                        SecurityConfig.class
                })
        })
class SellerVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SellerVerificationRepository sellerVerificationRepository;

    @MockitoBean
    private SellerVerificationMapper sellerVerificationMapper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SellerVerificationService sellerVerificationService;

    @Test
    @DisplayName("Deve submeter verificação com sucesso e retornar 201 Created")
    void submitVerification_WithValidPayload_ReturnsCreated() throws Exception {

        UserEntity mockUser = new UserEntity();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("John Doe");
        mockUser.setEmail("john@rdr.com");
        mockUser.setRole(Role.USER);

        CustomUserDetails customUserDetails = new CustomUserDetails(mockUser);

        SellerVerificationRequestDTO dto = new SellerVerificationRequestDTO(
                "John Doe",
                "73403536123",
                "21999999999",
                "73403536",
                "Módulo L, Condomínio Mestre D'Armas",
                ""
        );

        SellerVerificationEntity mockEntity = new SellerVerificationEntity();
        mockEntity.setId(UUID.randomUUID());
        mockEntity.setFullName("John Doe");
        mockEntity.setStatus(VerificationStatus.PENDING);

        SellerVerificationResponseDTO responseDto = new SellerVerificationResponseDTO(
                mockEntity.getId(),
                "John Doe",
                "73403536123",
                VerificationStatus.PENDING,
                Instant.now(),
                null,
                null,
                0,
                1
        );

        when(sellerVerificationService.submitVerification(any(UserEntity.class), any(SellerVerificationRequestDTO.class)))
                .thenReturn(mockEntity);

        when(sellerVerificationMapper.toResponseDTO(any(SellerVerificationEntity.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/seller/verifications")
                .with(user(customUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }



    @Test
    @DisplayName("Deve falhar ao submeter verificação com dados inválidos e retornar 400 Bad Request")
    void submitVerification_WithInvalidPayload_ReturnsBadRequest() throws Exception {

        UserEntity mockUser = new UserEntity();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("John Doe");
        mockUser.setEmail("john@rdr.com");
        mockUser.setRole(Role.USER);

        CustomUserDetails customUserDetails = new CustomUserDetails(mockUser);

        SellerVerificationEntity mockEntity = new SellerVerificationEntity();
        mockEntity.setId(UUID.randomUUID());
        mockEntity.setFullName("John Doe");
        mockEntity.setStatus(VerificationStatus.PENDING);


        SellerVerificationRequestDTO invalidDto = new SellerVerificationRequestDTO(
                "John Doe",
                "734035", // cpf incorreto
                "21999999999",
                "73403536",
                "Módulo L, Condomínio Mestre D'Armas",
                ""
        );

        when(sellerVerificationService.submitVerification(any(UserEntity.class), any(SellerVerificationRequestDTO.class)))
                .thenReturn(mockEntity);


        mockMvc.perform(post("/seller/verifications")
                .with(user(customUserDetails))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)));

    }

    @Test
    @DisplayName("Deve retornar histórico de verificações com status 200 OK")
    void getMyVerificationHistory_WhenCalled_ReturnsOkWithList() throws Exception {

        UserEntity mockUser = new UserEntity();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("John Doe");
        mockUser.setEmail("john@rdr.com");
        mockUser.setRole(Role.USER);

        CustomUserDetails customUserDetails = new CustomUserDetails(mockUser);

        SellerVerificationEntity entity1 = new SellerVerificationEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setFullName("John Doe 1");
        entity1.setStatus(VerificationStatus.PENDING);

        SellerVerificationResponseDTO dto1 = new SellerVerificationResponseDTO(
                entity1.getId(), "John Doe 1", "73403536123", VerificationStatus.PENDING,
                Instant.now(), null, null, 0, 1
        );

        SellerVerificationEntity entity2 = new SellerVerificationEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setFullName("John Doe 2");
        entity2.setStatus(VerificationStatus.APPROVED);

        SellerVerificationResponseDTO dto2 = new SellerVerificationResponseDTO(
                entity2.getId(), "John Doe 2", "73403536123", VerificationStatus.APPROVED,
                Instant.now(), Instant.now(), null, 0, 2
        );

        when(sellerVerificationService.getMyVerificationHistory(any(UUID.class)))
                .thenReturn(List.of(entity1, entity2));

        when(sellerVerificationMapper.toResponseDTO(any(SellerVerificationEntity.class)))
                .thenReturn(dto1, dto2);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                customUserDetails.getAuthorities()
        );

        mockMvc.perform(get("/seller/verifications/me")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("John Doe 1"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].fullName").value("John Doe 2"))
                .andExpect(jsonPath("$[1].status").value("APPROVED"));
}
}