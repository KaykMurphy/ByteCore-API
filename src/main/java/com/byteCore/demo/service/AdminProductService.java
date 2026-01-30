package com.byteCore.demo.service;

import com.byteCore.demo.domain.Product;
import com.byteCore.demo.dto.mapper.ProductMapper;
import com.byteCore.demo.dto.request.ProductCreateDTO;
import com.byteCore.demo.dto.request.ProductUpdateDTO;
import com.byteCore.demo.dto.response.ProductResponseDTO;
import com.byteCore.demo.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public ProductResponseDTO getProduct(Long id) {
        log.info("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product lookup failed. ID {} not found", id);
                    return new EntityNotFoundException("Product not found with id " + id);
                });

        return productMapper.toDto(product);
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO dto){
        log.info("Starting creation of new product: '{}'", dto.title());

        Product product = productMapper.toEntity(dto);

        product = productRepository.save(product);
        log.info("Product created successfully with ID: {}", product.getId());

        return productMapper.toDto(product);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO dto){
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed. Product with ID {} not found", id);
                    return new EntityNotFoundException("Product not found with id: " + id);
                });


        productMapper.updateEntityFromDto(dto, product);

        product = productRepository.save(product);
        log.info("Product with ID {} updated successfully", id);

        return productMapper.toDto(product);

    }

    @Transactional
    public void deleteProduct(Long id){
        log.info("Request to soft-delete product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Delete failed. Product with ID {} not found", id);
                    return new EntityNotFoundException("Product not found with id: " + id);
                });


        product.setActive(false);
        productRepository.save(product);

        log.info("Product with ID {} deactivated successfully", id);

    }
}

