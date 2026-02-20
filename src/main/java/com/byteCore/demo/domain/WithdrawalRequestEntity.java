package com.byteCore.demo.domain;

import com.byteCore.demo.enums.WithdrawalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "withdrawal_requests")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private UserEntity seller;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WithdrawalStatus status =  WithdrawalStatus.PENDING;


    @Column(nullable = false, length = 100)
    private String pixKey; // chave pix do vendedor

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant requestedAt;

    @Column
    private Instant processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private UserEntity processedBy; // admin que aprovou/rejeitou

    @Lob
    private String adminNotes;

    @Column(length = 200)
    private String transactionId; // ID da transacao PIX ( se aprovado)

    public void approve(UserEntity admin, String transactionId) {
        if (status != WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Only PENDING withdrawals can be approved");
        }
        this.status = WithdrawalStatus.APPROVED;
        this.processedAt = Instant.now();
        this.processedBy = admin;
        this.transactionId = transactionId;
    }

    public void reject(UserEntity admin, String reason) {
        if (status != WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Only PENDING withdrawals can be rejected");
        }

        if (reason == null || reason.isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        this.status = WithdrawalStatus.REJECTED;
        this.processedAt = Instant.now();
        this.processedBy = admin;
        this.adminNotes = reason;
    }

    public void markAsCompleted() {
        if (status != WithdrawalStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED withdrawals can be completed");
        }
        this.status = WithdrawalStatus.COMPLETED;
    }
}
