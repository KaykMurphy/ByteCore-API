package com.byteCore.demo.repository;

import com.byteCore.demo.domain.ProductStockEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStockEntity, Long> {

    @Query("SELECT ps FROM ProductStockEntity ps " +
            "WHERE ps.product.id = :productId " +
            "AND ps.available = true " +
            "AND ps.sold = false " +
            "ORDER BY ps.createdAt ASC")
    @Lock(LockModeType.PESSIMISTIC_WRITE)  // Previne race condition
    List<ProductStockEntity> findAvailableByProductId(
            @Param("productId") Long productId,
            Pageable pageable
    );

    @Query("SELECT COUNT(ps) FROM ProductStockEntity ps " +
            "WHERE ps.product.id = :productId " +
            "AND ps.available = true " +
            "AND ps.sold = false")
    Long countAvailableByProductId(@Param("productId") Long productId);

    List<ProductStockEntity> findByProductId(Long productId);

    List<ProductStockEntity> findBySoldTrue();

    List<ProductStockEntity> findByAvailableTrueAndSoldFalse();
}
