package com.byteCore.demo.repository;

import com.byteCore.demo.domain.DocumentEntity;
import com.byteCore.demo.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {


   List<DocumentEntity> findBySellerVerificationId(UUID verificationId);

   List<DocumentEntity> findBySellerVerificationIdAndDocumentType(UUID verificationId, DocumentType documentType);

   Long countBySellerVerificationId(UUID verificationId);

   void deleteBySellerVerificationId(UUID verificationId);}

