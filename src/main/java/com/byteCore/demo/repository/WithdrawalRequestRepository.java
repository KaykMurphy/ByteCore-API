package com.byteCore.demo.repository;

import com.byteCore.demo.domain.WithdrawalRequestEntity;
import com.byteCore.demo.enums.WithdrawalStatus;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequestEntity, UUID> {


    List<WithdrawalRequestEntity> findBySellerIdOrderByRequestedAtDesc(UUID sellerId);

    List<WithdrawalRequestEntity> findByStatusOrderByRequestedAtAsc(WithdrawalStatus status);

    @Query("""
        SELECT COALESCE(SUM(w.amount), 0)
        FROM WithdrawalRequestEntity w
        WHERE w.seller.id = :sellerId
          AND w.requestedAt >= :startDate
          AND w.status = com.byteCore.demo.enums.WithdrawalStatus.APPROVED
        """)
    BigDecimal getTotalWithdrawnToday(@Param("sellerId") UUID sellerId,
                                      @Param("startDate") Instant startDate);

}

