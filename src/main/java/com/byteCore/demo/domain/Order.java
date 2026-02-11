package com.byteCore.demo.domain;

import com.byteCore.demo.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private PaymentEntity payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String deliveryEmail;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Instant paidAt;

    private Instant deliveredAt;

    private Instant completedAt;

    private Instant cancelledAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = Instant.now();
        calculateTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        calculateTotal();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        calculateTotal();
    }

    public void calculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        return totalAmount;
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING_PAYMENT ||
                status == OrderStatus.PAID;
    }

    public boolean isPaid() {
        return payment != null &&
                payment.getStatus() == com.byteCore.demo.enums.PaymentStatus.APPROVED;
    }

    public boolean isDelivered() {
        return deliveredAt != null;
    }

    public void markAsPaid() {
        this.status = OrderStatus.PAID;
        this.paidAt = Instant.now();
    }

    public void markAsDelivered() {
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    public void markAsCompleted() {
        this.status = OrderStatus.COMPLETED;
        this.completedAt = Instant.now();
    }
}