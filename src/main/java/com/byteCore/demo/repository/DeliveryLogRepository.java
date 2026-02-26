package com.byteCore.demo.repository;

import com.byteCore.demo.domain.DeliveryLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLogEntity, UUID> {
}
