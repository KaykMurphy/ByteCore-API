package com.byteCore.demo.domain;

import com.byteCore.demo.enums.ProductStatus;
import com.byteCore.demo.enums.ProductType;
import com.byteCore.demo.enums.DeliveryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products")
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Version
    private Long version;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductType type;

    @Column(length = 100)
    private String platform;

    @Column(length = 50)
    private String region = "GLOBAL";

    @Column(nullable = false)
    private Long availableStock = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryType deliveryType = DeliveryType.MANUAL;

    @Column
    private Integer estimatedDeliveryMinutes = 5;

    @Column(columnDefinition = "TEXT")
    private String activationInstructions;

    @Column(columnDefinition = "TEXT")
    private String importantNotes;

    @Column(nullable = false)
    private Long totalSold = 0L;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column
    private Instant submittedForReviewAt;

    @Column
    private Instant approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    public boolean hasStock(int quantity) {
        return availableStock != null && availableStock >= quantity;
    }

    public void decrementStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Estoque insuficiente");
        }
        this.availableStock -= quantity;
    }

    public void incrementStock(int quantity) {
        this.availableStock += quantity;
    }

    public void incrementSales(int quantity) {
        this.totalSold += quantity;
    }

    public boolean isAvailableForSale() {
        return (
                active &&
                        status == ProductStatus.APPROVED &&
                        (deliveryType == DeliveryType.MANUAL || hasStock(1))
        );
    }


    // Vendedor submete produto para análise
    public void submitForReview() {

        if (status != ProductStatus.DRAFT) {
            throw new IllegalStateException("Apenas rascunhos podem ser enviados");
        }

        this.status = ProductStatus.PENDING_REVIEW;
        this.submittedForReviewAt = Instant.now();
    }

    // Admin aprova produto
    public void approveProduct(User admin) {

        if (status != ProductStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Apenas produtos pendentes podem ser aprovados");
        }

        this.status = ProductStatus.APPROVED;
        this.approvedAt = Instant.now();
        this.approvedBy = admin;
        this.rejectionReason = null;
        this.active = true;
    }

    // Admin rejeita produto
    public void rejectProduct(User admin, String reason) {

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Motivo é obrigatório");
        }

        if (status != ProductStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Apenas produtos pendentes podem ser rejeitados");
        }

        this.status = ProductStatus.REJECTED;
        this.rejectionReason = reason;
        this.approvedBy = admin;
        this.active = false;
    }

    // Vendedor pode editar?
    public boolean canBeEditedBySeller() {
        return status == ProductStatus.DRAFT
                || status == ProductStatus.REJECTED;
    }

    public boolean belongsToSeller(UUID sellerId) {

        if (seller == null) {
            return false;
        }

        return seller.getId().equals(sellerId);
    }
}
