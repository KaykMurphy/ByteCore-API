package com.byteCore.demo.repository;

import com.byteCore.demo.domain.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    @Query("SELECT ps FROM ProductStock ps " +
            "WHERE ps.product.id = :productId " +
            "AND ps.available = true " +
            "AND ps.sold = false " +
            "ORDER BY ps.createdAt ASC")
    List<ProductStock> findAvailableByProductId(
            @Param("productId") Long productId,
            @Param("limit") int limit
    );

    @Query("SELECT COUNT(ps) FROM ProductStock ps " +
            "WHERE ps.product.id = :productId " +
            "AND ps.available = true " +
            "AND ps.sold = false")
    Long countAvailableByProductId(@Param("productId") Long productId);

    List<ProductStock> findByProductId(Long productId);

    List<ProductStock> findBySoldTrue();

    List<ProductStock> findByAvailableTrueAndSoldFalse();
}