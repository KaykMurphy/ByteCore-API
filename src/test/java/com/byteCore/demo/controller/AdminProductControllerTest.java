package com.byteCore.demo.controller;

import com.byteCore.demo.config.SecurityConfig;
import com.byteCore.demo.dto.request.ProductCreateDTO;
import com.byteCore.demo.dto.request.ProductUpdateDTO;
import com.byteCore.demo.dto.response.ProductResponseDTO;
import com.byteCore.demo.enums.ProductType;
import com.byteCore.demo.security.JwtAuthenticationFilter;
import com.byteCore.demo.service.AdminProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminProductController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        JwtAuthenticationFilter.class,
                        SecurityConfig.class
                })
        }
)

@AutoConfigureMockMvc(addFilters = false)
public class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminProductService adminProductService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /admin/products - Should return 201 Created and Product Response")
    void createProduct_shouldReturn201_whenDataIsValid() throws Exception {

        ProductCreateDTO requestDTO = new ProductCreateDTO(
                "Red Dead Redemption 2",
                "Jogo de ação e aventura épico no velho oeste.",
                new BigDecimal("150.50"),
                "https://imagem.com/rdr2-cover.jpg",
                ProductType.GAME_KEY,
                "Steam",
                "Global",
                50L,
                "Abra a Steam, vá em 'Jogos' e clique em 'Ativar um Produto no Steam...'",
                "É necessário ter uma conta na Rockstar Games Social Club para jogar.",
                20
        );

        ProductResponseDTO responseDTO = new ProductResponseDTO(
                1L,
                "Red Dead Redemption 2",
                "Jogo de ação e aventura épico no velho oeste.",
                new BigDecimal("150.50"),
                "https://imagem.com/rdr2-cover.jpg",
                50L,
                ProductType.GAME_KEY,
                null,
                null
        );

        when(adminProductService.createProduct(any(ProductCreateDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Red Dead Redemption 2"))
                .andExpect(jsonPath("$.description").value("Jogo de ação e aventura épico no velho oeste."))
                .andExpect(jsonPath("$.price").value(150.50))
                .andExpect(jsonPath("$.imageUrl").value("https://imagem.com/rdr2-cover.jpg"))
                .andExpect(jsonPath("$.availableStock").value(50))
                .andExpect(jsonPath("$.type").value("GAME_KEY"));
    }

    @Test
    @DisplayName("POST /admin/products - Should return 400 bad request when title is null")
    void createProduct_shouldReturn400_whenTitleIsNull() throws Exception {

        ProductCreateDTO requestDTO = new ProductCreateDTO(
                null,
                "Jogo de ação e aventura épico no velho oeste.",
                new BigDecimal("150.50"),
                "https://imagem.com/rdr2-cover.jpg",
                ProductType.GAME_KEY,
                "Steam",
                "Global",
                50L,
                "Abra a Steam, vá em 'Jogos' e clique em 'Ativar um Produto no Steam...'",
                "É necessário ter uma conta na Rockstar Games Social Club para jogar.",
                20
        );

        mockMvc.perform(post("/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    @DisplayName("PUT /admin/products/{id} - Should return 200 OK when data is valid")
    void updateProduct_shouldReturn200OK_whenDataIsValid() throws Exception {

        Long id = 1L;

        ProductUpdateDTO updateDTO = new ProductUpdateDTO(
                "Red Dead Redemption 2 - Ultimate Edition",
                null, null, null, null, null, null, null, null, null, null, null, null
        );

        ProductResponseDTO responseDTO = new ProductResponseDTO(
                1L,
                "Red Dead Redemption 2 - Ultimate Edition",
                "Jogo de ação e aventura épico no velho oeste.",
                new BigDecimal("150.50"),
                "https://imagem.com/rdr2-cover.jpg",
                50L,
                ProductType.GAME_KEY,
                null,
                null
        );


        when(adminProductService.updateProduct(eq(id), any(ProductUpdateDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/admin/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Red Dead Redemption 2"))
                .andExpect(jsonPath("$.description").value("Jogo de ação e aventura épico no velho oeste."))
                .andExpect(jsonPath("$.price").value(150.50))
                .andExpect(jsonPath("$.imageUrl").value("https://imagem.com/rdr2-cover.jpg"))
                .andExpect(jsonPath("$.availableStock").value(50))
                .andExpect(jsonPath("$.type").value("GAME_KEY"));
    }

    @Test
    @DisplayName("PUT /admin/products/{id} - Should return 400 Bad Request when JSON format is invalid")
    void updateProduct_shouldReturn400_whenJsonFormatIsInvalid() throws Exception {

        Long id = 1L;

        String invalidJson = """
                {
                    "title": "Red Dead Redemption 2",
                    "price": "mil e quinhentos reais"
                }
                """;

        mockMvc.perform(put("/admin/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Malformed JSON request"))
                .andExpect(jsonPath("$.path").value("/admin/products/1"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("DELETE /admin/products/{id} - Should return 204 No Content when product exists")
    void deleteProduct_shouldReturnNoContent_whenProductExists() throws Exception {

        Long id = 20L;

        doNothing().when(adminProductService).deleteProduct(id);

        mockMvc.perform(delete("/admin/products/{id}", id))
                .andExpect(status().isNoContent());
    }


}
