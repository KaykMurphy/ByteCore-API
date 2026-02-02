package com.byteCore.demo;

import com.byteCore.demo.domain.Product;
import com.byteCore.demo.dto.mapper.ProductMapper;
import com.byteCore.demo.dto.request.ProductCreateDTO;
import com.byteCore.demo.dto.response.ProductResponseDTO;
import com.byteCore.demo.enums.ProductType;
import com.byteCore.demo.repository.ProductRepository;
import com.byteCore.demo.service.AdminProductService;
import com.byteCore.demo.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper  productMapper;

    @Mock
    private ProductService productService;

    @InjectMocks
    private AdminProductService adminProductService;


    @Test
    void findById_shouldReturnProductResponseDTO_whenProductExists() {

        Long productId = 1L;

        Product product = new Product();
        product.setTitle("Title admin");
        product.setId(productId);
        product.setActive(false);
        product.setType(ProductType.SOFTWARE);

        ProductResponseDTO responseDTO =
                new ProductResponseDTO(
                        productId,
                        "Title admin",
                        "Descrição",
                        null,
                        "img.png",
                        null,
                        ProductType.SOFTWARE
                );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(productMapper.toDto(product))
                .thenReturn(responseDTO);

        // act
        ProductResponseDTO result =
                adminProductService.getProduct(productId);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId);
        assertThat(result.title()).isEqualTo("Title admin");

        verify(productRepository, times(1))
                .findById(productId);

        verify(productMapper, times(1))
                .toDto(product);
    }

    @Test
    void shouldThrowEntityNotFoundException_whenProductDoesNotExists() {

        Long productId = 1L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.getProduct(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found with id "+productId);

        verify(productRepository, times(1))
                .findById(productId);

        verify(productMapper, never())
                .toDto(any());
    }


    @Test
    void createProduct_shouldCreateProductSuccessfully() {

        ProductCreateDTO createDTO = new ProductCreateDTO(
                "Notebook",
                "Notebook Gamer",
                new BigDecimal("4500.00"),
                "img.png",
                ProductType.SOFTWARE
        );

        Product product = new Product();
        product.setTitle("Notebook");
        product.setDescription("Notebook Gamer");
        product.setPrice(new BigDecimal("4500.00"));

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setTitle("Notebook");
        savedProduct.setDescription("Notebook Gamer");
        savedProduct.setType(ProductType.SOFTWARE);
        savedProduct.setPrice(new BigDecimal("4500.00"));
        savedProduct.setImageUrl("img.png");

        ProductResponseDTO response = new ProductResponseDTO(
                1L,
                "Notebook",
                "Notebook Gamer",
                new BigDecimal("4500.00"),
                "img.png",
                null,
                ProductType.SOFTWARE
        );

        // Arrange
        when(productMapper.toEntity(createDTO)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productMapper.toDto(savedProduct)).thenReturn(response);

        // Act
        ProductResponseDTO result = adminProductService.createProduct(createDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Notebook", result.title());
        assertEquals("Notebook Gamer", result.description());
        assertEquals(new BigDecimal("4500.00"), result.price());

        // Verify
        verify(productMapper, times(1)).toEntity(createDTO);
        verify(productRepository, times(1)).save(product);
        verify(productMapper, times(1)).toDto(savedProduct);

        verifyNoMoreInteractions(productMapper, productRepository);
    }



}
