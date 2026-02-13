package com.byteCore.demo.repository;

import com.byteCore.demo.domain.Product;
import com.byteCore.demo.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrueAndStatus(ProductStatus status, Pageable pageable);
    List<Product> findByTitleContainingIgnoreCaseAndActiveTrue(String title);

    Page<Product> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);

    Page<Product> findBySellerIdAndStatusOrderByCreatedAtDesc(UUID sellerId, ProductStatus status, Pageable pageable);

    Page<Product> findByStatusOrderBySubmittedForReviewAtAsc(ProductStatus status, Pageable pageable);

    Optional<Product> findByIdAndSellerId(Long productId, UUID sellerId);

    public Long countByStatus(ProductStatus status);

    public List<Product> findByTitleContainingIgnoreCaseAndStatusAndActiveTrue(String title, ProductStatus status);
}

