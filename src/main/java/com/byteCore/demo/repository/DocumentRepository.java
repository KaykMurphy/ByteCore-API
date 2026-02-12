package com.byteCore.demo.repository;

import com.byteCore.demo.domain.Document;
import com.byteCore.demo.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.print.Doc;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

   List<Document> findBySellerVerificationId(Long verificationId);

   List<Document> findBySellerVerificationIdAndDocumentType(Long verificationId, DocumentType documentType);

   Long countBySellerVerificationId(Long verificationId);

   void deleteBySellerVerificationId(Long verificationId);}

