package com.byteCore.demo.domain;

import com.byteCore.demo.enums.ProductType;
import com.byteCore.demo.enums.DeliveryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ═══════════════════════════════════════════════════════════════
 *  PRODUTO DIGITAL
 * ═══════════════════════════════════════════════════════════════
 *
 * Representa produtos digitais como:
 * - Keys de jogos (Steam, Origin, Epic, etc)
 * - Contas (Netflix, Spotify, LoL, etc)
 * - Assinaturas
 * - Discord Nitro
 * - Gift Cards
 */
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

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductType type;

    @Column(length = 100)
    private String platform;

    @Column(length = 50)
    @Builder.Default
    private String region = "GLOBAL";

    @Column(nullable = false)
    @Builder.Default
    private Long availableStock = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DeliveryType deliveryType = DeliveryType.MANUAL;

    @Column
    @Builder.Default
    private Integer estimatedDeliveryMinutes = 5;

    @Column(columnDefinition = "TEXT")
    private String activationInstructions;

    @Column(columnDefinition = "TEXT")
    private String importantNotes;

    @Column(nullable = false)
    @Builder.Default
    private Long totalSold = 0L;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

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
        return active && (deliveryType == DeliveryType.MANUAL || hasStock(1));
    }
}