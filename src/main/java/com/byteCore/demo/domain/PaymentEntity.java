package com.byteCore.demo.domain;

import com.byteCore.demo.enums.PaymentMethod;
import com.byteCore.demo.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String externalId; // ID do Mercado Pago

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(columnDefinition = "TEXT")
    private String qrCode;

    @Column(columnDefinition = "TEXT")
    private String pixQrCode;

    @Column(columnDefinition = "TEXT")
    private String pixQrCodeText;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private OrderEntity order;

    @Column
    private Instant moneyReleaseDate; // quando será liberado

    @Column
    private Instant moneyReleasedAt; // quando foi liberado

    @Column(nullable = false)
    private boolean moneyReleased = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal sellerAmount; // quanto que o vendedor vai receber


    public void calculateReleaseDate(boolean hasGoodReview){

        if(paidAt == null) {
            throw new IllegalStateException("Cannot calculate release date without payment date");
        }

        // Previne recálculo
        if (moneyReleaseDate != null) {
            return;
        }

        if (hasGoodReview) {
            moneyReleaseDate = paidAt.plus(7, ChronoUnit.DAYS);
        }
        else {
            moneyReleaseDate = paidAt.plus(14, ChronoUnit.DAYS);
        }

            this.sellerAmount = this.amount;
    }

    public void markAsReleased() {
        moneyReleased = true;
        moneyReleasedAt = Instant.now();
    }

    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
