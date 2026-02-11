package com.byteCore.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(columnDefinition = "TEXT")
    private String deliveredContent;

    private Instant deliveredAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean delivered = false;

    @PrePersist
    @PreUpdate
    protected void calculateSubtotal() {
        if (price != null && quantity != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getSubtotal() {
        if (subtotal == null) {
            calculateSubtotal();
        }
        return subtotal;
    }

    public void markAsDelivered(String content) {
        this.deliveredContent = content;
        this.delivered = true;
        this.deliveredAt = Instant.now();
    }
}