package com.byteCore.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"order_id", "reviewer_id"} // combinacao unica
        ),
        indexes = {
                @Index(name = "idx_reviews_reviewed_user", columnList = "reviewed_user_id")
        }
)
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private UserEntity reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_user_id",  nullable = false)
    private UserEntity reviewedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer rating; // estrelas

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000)
    private String comment;

    @Column(nullable = false)
    private boolean verifiedPurchase = true;

    @Column(nullable = false)
    private Integer editCount = 0; // máximo 2 edicoes

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @Version
    private Long version;

    private void setEditCount(Integer count) {
        if (count != null && count > 2) {
            throw new IllegalStateException("Edit count cannot exceed 2");
        }
        this.editCount = count;

    }

    public void setRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalStateException("Rating must be between 1 and 5");
        }

        this.rating = rating;
    }

    public void updateReview(Integer newRating, String newComment) {
        if (!canEdit()) {
            throw new IllegalStateException("Cannot edit review anymore");
        }

        setRating(newRating);
        this.comment = newComment;
        incrementEditCount();
    }


    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public boolean canEdit() {
        return editCount < 2;
    }

    public void incrementEditCount() {
        if (canEdit()) {
            this.editCount++;
        }else {
            throw new IllegalStateException("Can't edit review");
        }
    }
}

