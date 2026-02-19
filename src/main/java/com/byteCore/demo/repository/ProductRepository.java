package com.byteCore.demo.repository;

import com.byteCore.demo.domain.ProductEntity;
import com.byteCore.demo.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Page<ProductEntity> findByActiveTrueAndStatus(ProductStatus status, Pageable pageable);
    List<ProductEntity> findByTitleContainingIgnoreCaseAndActiveTrue(String title);

    Page<ProductEntity> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);

    Page<ProductEntity> findBySellerIdAndStatusOrderByCreatedAtDesc(UUID sellerId, ProductStatus status, Pageable pageable);

    Page<ProductEntity> findByStatusOrderBySubmittedForReviewAtAsc(ProductStatus status, Pageable pageable);

    Optional<ProductEntity> findByIdAndSellerId(Long productId, UUID sellerId);

    public Long countByStatus(ProductStatus status);

    public List<ProductEntity> findByTitleContainingIgnoreCaseAndStatusAndActiveTrue(String title, ProductStatus status);
}

