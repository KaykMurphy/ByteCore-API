package com.byteCore.demo;

import com.byteCore.demo.domain.Product;
import com.byteCore.demo.dto.mapper.ProductMapper;
import com.byteCore.demo.dto.response.ProductResponseDTO;
import com.byteCore.demo.repository.ProductRepository;
import com.byteCore.demo.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductMapper  productMapper;

    @InjectMocks
    ProductService productService;

    @Test
    void findById_shouldReturnProductResponseDTO_whenProductExistsAndIsActive(){

        Long productId = 1L;

        Product product = new Product();
        product.setId(productId);
        product.setTitle("title");
        product.setActive(true);

        ProductResponseDTO responseDTO =
                new ProductResponseDTO(
                        productId,
                        "Produto Ativo",
                        "Descrição",
                        null,
                        "img.png",
                        null
                );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(productMapper.toDto(product))
                .thenReturn(responseDTO);


        //act
        ProductResponseDTO result
                = productService.findById(productId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId);
        assertThat(result.title()).isEqualTo("Produto Ativo");

        verify(productRepository, times(1))
                .findById(productId);

        verify(productMapper, times(1))
                .toDto(product);
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenProductDoesNotExist() {
        Long productId = 1L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found with id "+productId);

        verify(productRepository, times(1))
                .findById(productId);

        verify(productMapper, never())
                .toDto(any());
    }

    @Test
    void findById_shouldThrowIllegalArgumentException_whenProductIsInactive() {

        Long productId = 1L;

        Product product = new Product();
        product.setId(productId);
        product.setActive(false);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));


        assertThatThrownBy(() ->
                productService.findById(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product is not active");

        verify(productRepository, times(1))
                .findById(productId);

        verify(productMapper, never())
                .toDto(any());
    }
}
