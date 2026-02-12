package com.byteCore.demo.domain;

import com.byteCore.demo.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id", nullable = false)
    private SellerVerification sellerVerification;  // A qual verificação pertence

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentType documentType;

    @Column(nullable = false, length = 255)
    private String fileName; // nome original do arquivo

    @Column(nullable = false, length = 500)
    private String storedPath; // caminho onde foi salvo no servidor/s3

    @Column(nullable = false)
    private Long fileSizeBytes;

    @Column(length = 50)
    private String mimeType;  // image/jpeg, application/pdf, etc

    @Column(nullable = false)
    private boolean verified = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    @Column
    private Instant verifiedAt;

    @Column(columnDefinition = "TEXT")
    private String adminNotes; // notas do admin sobre o documento

    public void markAsVerified(String adminNotes) {
        this.verified = true;
        this.verifiedAt = Instant.now();
        this.adminNotes = adminNotes;
    }

    public Boolean isExpired() {
        if (this.uploadedAt == null) return true; // sem data > expirado

        // > 90 dias
        Instant cutoffDate = Instant.now().minus(90, ChronoUnit.DAYS);
        return this.uploadedAt.isBefore(cutoffDate);
    }
}

