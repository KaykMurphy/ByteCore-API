package com.byteCore.demo.repository;

import com.byteCore.demo.domain.PaymentEntity;
import com.byteCore.demo.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByExternalId(String id);

    List<PaymentEntity> findByStatusAndMoneyReleasedFalseAndMoneyReleaseDateBefore(
            PaymentStatus status,
            Instant releaseDate
    );

}
