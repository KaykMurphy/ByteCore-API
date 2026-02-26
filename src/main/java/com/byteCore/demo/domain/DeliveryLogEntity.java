package com.byteCore.demo.domain;

import com.byteCore.demo.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Builder
@Entity
@Table(name = "delivery_logs")
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Integer attemptCount = 1;

    @CreationTimestamp
    private Instant attemptedAt;

    private Instant nextRetryAt;
}
