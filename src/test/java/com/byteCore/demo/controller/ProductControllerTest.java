package com.byteCore.demo.controller;

import com.byteCore.demo.config.SecurityConfig;
import com.byteCore.demo.dto.response.ProductResponseDTO;
import com.byteCore.demo.enums.ProductType;
import com.byteCore.demo.security.JwtAuthenticationFilter;
import com.byteCore.demo.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.when;

@WebMvcTest(
        controllers = ProductController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        JwtAuthenticationFilter.class,
                        SecurityConfig.class
                })
        }
)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("GET /api/products/{id} - Should return 200 and product data")
    void getProductById_shouldReturn200AndProductData() throws Exception {

        Long productId = 1L;

        ProductResponseDTO mockResponse = new ProductResponseDTO(
                productId,
                "Red Dead Redemption 2",
                "Jogo de faroeste",
                new BigDecimal("150.00"),
                "url-da-imagem.jpg",
                10L,
                ProductType.GAME_KEY,
                 UUID.randomUUID(),
                "Loja do Dutch"
        );

        when(productService.findById(productId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.title").value("Red Dead Redemption 2"))
                .andExpect(jsonPath("$.price").value(150.00))
                .andExpect(jsonPath("$.sellerName").value("Loja do Dutch"));
    }



}
