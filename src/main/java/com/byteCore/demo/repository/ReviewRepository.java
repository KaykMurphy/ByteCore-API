package com.byteCore.demo.repository;

import com.byteCore.demo.domain.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    List<ReviewEntity> findByReviewedUserId(UUID userId);

    List<ReviewEntity> findByOrderId(Long orderId);

    boolean existsByOrderIdAndReviewerId(Long orderId, UUID reviewerId);

    Optional<ReviewEntity> findByOrderIdAndReviewerId(Long orderId, UUID reviewerId);

    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.reviewedUser.id = :userId")
    Double getAverageRatingByUserId(@Param("userId")UUID userId);

    Long countByReviewedUserId(UUID userId);
}
