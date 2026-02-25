package com.byteCore.demo.repository;

import com.byteCore.demo.domain.BannedDocumentEntity;
import com.byteCore.demo.enums.DocumentType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BannedDocumentRepository extends JpaRepository<BannedDocumentEntity, UUID> {

    boolean existsByDocumentHash(String hash);

    Optional<BannedDocumentEntity> findByDocumentHash(String hash);

    List<BannedDocumentEntity> findByBannedUserId(UUID userId);

    List<BannedDocumentEntity> findByDocumentType(DocumentType documentType);

}
