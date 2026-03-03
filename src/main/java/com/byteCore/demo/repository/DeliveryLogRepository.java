package com.byteCore.demo.repository;

import com.byteCore.demo.domain.DeliveryLogEntity;
import com.byteCore.demo.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLogEntity, UUID> {


    List<DeliveryLogEntity> findByStatusAndNextRetryAtBefore(
            DeliveryStatus status,
            Instant now
    );



}
