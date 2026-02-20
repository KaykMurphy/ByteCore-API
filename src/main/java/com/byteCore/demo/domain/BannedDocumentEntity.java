package com.byteCore.demo.domain;

import com.byteCore.demo.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "banned_documents",
        indexes = {
                @Index(name = "idx_document_hash", columnList = "documentHash")
        }
)
public class BannedDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String documentHash; // SHA-256 em hexadecimal

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType; // RG, CPF...

    @Column(nullable = false)
    private UUID bannedUserId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant bannedAt;

    @Column(nullable = false)
    private String bannedByAdminEmail;
}


