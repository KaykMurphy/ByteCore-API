package com.byteCore.demo.service;

import com.byteCore.demo.domain.ProductEntity;
import com.byteCore.demo.dto.mapper.ProductMapper;
import com.byteCore.demo.dto.request.ProductCreateDTO;
import com.byteCore.demo.dto.request.ProductUpdateDTO;
import com.byteCore.demo.dto.response.ProductResponseDTO;
import com.byteCore.demo.enums.ProductStatus;
import com.byteCore.demo.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.TestDataFactory;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper  productMapper;

    @InjectMocks
    private AdminProductService adminProductService;

    @Test
    @DisplayName("Find by id should return product responseDTO when product exists")
    void findById_shouldReturnProductResponseDTO_whenProductExists() {

        Long productId = 1L;
        UUID mockSellerId = UUID.randomUUID();

        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(productId);

        ProductResponseDTO responseDTO = new ProductResponseDTO(
                productId,
                null,
                null,
                null,
                null,
                null,
                null,
                mockSellerId,
                null
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(productEntity));

        when(productMapper.toDto(productEntity))
                .thenReturn(responseDTO);

        //act
        ProductResponseDTO result =
                adminProductService.getProduct(productId);

        //assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId);

        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).toDto(productEntity);
    }


    @Test
    @DisplayName("Should throw exception when product does not exist")
    void get_Product_shouldThrowException_whenProductDoesNotExist() {

        Long productId = 1L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.getProduct(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found with id: " + productId);

        verify(productRepository, times(1))
                .findById(productId);

    }

    @Test
    @DisplayName("Should create product successfully")
    void createProduct_shouldCreateProductSuccessfully() {

        Long productId = 1L;
        ProductCreateDTO dto = TestDataFactory.validProductCreateDTO();

        ProductEntity  productEntity = new ProductEntity();
        productEntity.setTitle(dto.title());


        ProductEntity savedProductEntity = new  ProductEntity();
        savedProductEntity.setId(productId);
        savedProductEntity.setTitle(dto.title());


        ProductResponseDTO responseDTO = new ProductResponseDTO(
                productId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );


        when(productMapper.toEntity(dto))
                .thenReturn(productEntity);

        when(productRepository.save(productEntity))
                .thenReturn(savedProductEntity);

        when(productMapper.toDto(savedProductEntity))
                .thenReturn(responseDTO);

        //act
        ProductResponseDTO result = adminProductService.createProduct(dto);


        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId);

        verify(productMapper, times(1)).toEntity(dto);
        verify(productRepository, times(1)).save(productEntity);
        verify(productMapper, times(1)).toDto(savedProductEntity);

        assertThat(productEntity.getStatus()).isEqualTo(ProductStatus.APPROVED);
        assertThat(productEntity.getSeller()).isNull();
    }


    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_shouldUpdateProductSuccessfully() {

        Long productId = 1L;

        ProductUpdateDTO dto = TestDataFactory.validProductUpdateDTO();

        ProductEntity existingProduct = new ProductEntity();
        existingProduct.setId(productId);

        ProductEntity savedProductEntity = new ProductEntity();
        savedProductEntity.setId(productId);

        ProductResponseDTO responseDTO = new ProductResponseDTO(
                productId, null, null, null, null, null, null, null, null
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(existingProduct))
                .thenReturn(savedProductEntity);

        when(productMapper.toDto(savedProductEntity))
                .thenReturn(responseDTO);

        // act
        ProductResponseDTO result = adminProductService.updateProduct(productId, dto);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(productId);

        // verify
        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).updateEntityFromDto(dto, existingProduct);
        verify(productRepository, times(1)).save(existingProduct);
        verify(productMapper, times(1)).toDto(savedProductEntity);

    }


    @Test
    @DisplayName("Should throw exception when product does not exist")
    void updateProduct_shouldThrowException_whenProductDoesNotExist() {
        Long productId = 1L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.updateProduct(
                productId, TestDataFactory.validProductUpdateDTO()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found with id: " + productId);

        verify(productRepository, times(1)).findById(productId);

    }


    @Test
    @DisplayName("Should delete product successfully")
    void  deleteProduct_shouldDeleteProductSuccessfully() {
        Long productId = 1L;

        ProductEntity  product = new ProductEntity();
        product.setId(productId);
        product.setActive(true);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));


        adminProductService.deleteProduct(productId);

        assertThat(product.isActive()).isFalse();

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Should throw exception when product does not exist")
    void deleteProduct_shouldThrowException_whenProductDoesNotExist() {

        Long productId = 1L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminProductService.deleteProduct(productId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found with id: " + productId);

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

}
