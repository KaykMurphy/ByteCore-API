package com.byteCore.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "product_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private boolean available = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean sold = false;

    private Instant soldAt;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private Instant addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = Instant.now();
    }

    public void markAsSold(OrderItem orderItem) {
        this.sold = true;
        this.available = false;
        this.soldAt = Instant.now();
        this.orderItem = orderItem;
    }

    public void markAsAvailable() {
        this.available = true;
        this.sold = false;
        this.soldAt = null;
        this.orderItem = null;
    }

    public boolean isAvailableForSale() {
        return available && !sold;
    }
}