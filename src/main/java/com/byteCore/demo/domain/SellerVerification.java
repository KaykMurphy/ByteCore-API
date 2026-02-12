package com.byteCore.demo.domain;

import com.byteCore.demo.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.print.Doc;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seller_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellerVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // quem está pedindo verificação

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Column(nullable = false, length = 8)
    private String cep;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo; // Info extra do vendedor

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(nullable = false)
    private Integer version = 1; // tentativa número X

    @OneToMany(mappedBy = "sellerVerification", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

    @Column
    private Instant reviewedAt; // quando admin analisou

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy; // qual admin aprovou/rejeitou

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column
    private Instant expiresAt; // quando documentos expiram

    public void addDocument(Document document) {
        documents.add(document);
        document.setSellerVerification(this);
    }

    public void removeDocument(Document document) {
        documents.remove(document);
        document.setSellerVerification(null);
    }

    public void approve(User admin) {
        status = VerificationStatus.APPROVED;
        reviewedAt = Instant.now();
        reviewedBy = admin;
        rejectionReason = null;
    }

    public void reject(User admin, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Motivo é obrigatório");
        }

        this.status = VerificationStatus.REJECTED;
        this.reviewedAt = Instant.now();
        this.reviewedBy = admin;
        this.rejectionReason = reason; // Atribua por último
    }

    public Boolean allDocumentsVerified() {

        if (documents.isEmpty()) {
            return false;
        }

        for (Document document : documents) {
            if (!document.isVerified()) {
                return false;
            }
        }

        return true;
    }

    public Integer getDocumentCount() {
        return documents.size();
    }

}

