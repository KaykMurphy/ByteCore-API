package com.byteCore.demo.controller;


import com.byteCore.demo.config.SecurityConfig;
import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.OrderMapper;
import com.byteCore.demo.dto.request.OrderCreateDTO;
import com.byteCore.demo.dto.request.OrderItemRequestDTO;
import com.byteCore.demo.dto.response.OrderItemResponseDTO;
import com.byteCore.demo.dto.response.OrderResponseDTO;
import com.byteCore.demo.enums.Role;
import com.byteCore.demo.security.CustomUserDetails;
import com.byteCore.demo.security.JwtAuthenticationFilter;
import com.byteCore.demo.service.OrderService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        JwtAuthenticationFilter.class,
                        SecurityConfig.class
                })
        }
)

@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderMapper orderMapper;

    @Test
    @DisplayName("POST /api/orders - Should return 201 Created and order data when valid")
    void create_shouldReturn201_whenDataIsValid() throws Exception {

        UserEntity mockUser = new UserEntity();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("comprador@gmail.com");
        mockUser.setRole(Role.USER);

        CustomUserDetails customUserDetails = new CustomUserDetails(mockUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        OrderItemRequestDTO item1 = new OrderItemRequestDTO();
        item1.setProductId(1L);
        item1.setQuantity(2);

        OrderItemRequestDTO item2 = new OrderItemRequestDTO();
        item2.setProductId(2L);
        item2.setQuantity(3);

        OrderCreateDTO requestDTO = new OrderCreateDTO();
        requestDTO.setItems(List.of(item1, item2));

        OrderEntity mockOrder = new OrderEntity();
        mockOrder.setId(500L);

        OrderItemResponseDTO responseItem1 = new OrderItemResponseDTO(
                100L,
                "Red Dead Redemption 2",
                2,
                new BigDecimal("150.00"),
                new BigDecimal("300.00"),
                true
        );

        OrderResponseDTO responseDTO = new OrderResponseDTO(
                500L,
                "PENDING_PAYMENT",
                new BigDecimal("300.00"),
                "comprador@gmail.com",
                Instant.now(),
                List.of(responseItem1)
        );

        when(orderService.createOrder(eq(mockUser), any(OrderCreateDTO.class)))
                .thenReturn(mockOrder);

        when(orderMapper.toResponseDTO(mockOrder))
                .thenReturn(responseDTO);

        try {
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))

                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(500)) // Sem o 'L' e usando .value()
                    .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                    .andExpect(jsonPath("$.totalAmount").value(300.00))
                    .andExpect(jsonPath("$.deliveryEmail").value("comprador@gmail.com"));
        } finally {
            SecurityContextHolder.clearContext();
        }

    }
}
