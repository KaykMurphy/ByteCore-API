package com.byteCore.demo.service;

import com.byteCore.demo.domain.Product;
import com.byteCore.demo.dto.mapper.ProductMapper;
import com.byteCore.demo.dto.response.ProductResponseDTO;
import com.byteCore.demo.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findlAll(Pageable pageable){

        log.info("Inside findlAll. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> products = productRepository.findAll(pageable);

        log.info("Found: {} products in current page", products.getTotalElements());

        return  products.map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id){

        log.info("Inside findById. Id: {} ", id);

        Product product = productRepository.findById(id).orElseThrow(() -> {
            log.warn("Product not found with id: {}", id);
            return new EntityNotFoundException("Product not found with id " + id);
        });

        if (!product.getActive()){
            log.warn("Product not found with id: {}", id);
            throw new IllegalArgumentException("Product is not active.");
        }

        return  productMapper.toDto(product);
    }


    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchByTitle(String query) {

        log.info("Inside searchByTitle. Query: {} ", query);

        List<Product> product = productRepository.findByTitleContainingIgnoreCaseAndActiveTrue(query);

        if (product.isEmpty()) {
            log.warn("Product not found with query: {}", query);
            throw new EntityNotFoundException("Product not found.");
        }

        log.debug("Search returned {} results", product.size());

        return product.stream()
                .map(productMapper::toDto)
                .toList();
    }


}
