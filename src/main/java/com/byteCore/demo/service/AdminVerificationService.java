package com.byteCore.demo.service;

import com.byteCore.demo.domain.DocumentEntity;
import com.byteCore.demo.domain.SellerVerificationEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.enums.Role;
import com.byteCore.demo.enums.VerificationStatus;
import com.byteCore.demo.repository.SellerVerificationRepository;
import com.byteCore.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminVerificationService {

    private final SellerVerificationRepository sellerVerificationRepository;
    private final UserRepository userRepository;
    private final BlacklistService blacklistService;

    public List<SellerVerificationEntity> listPendingVerifications() {
        return sellerVerificationRepository.findByStatusOrderBySubmittedAtAsc(VerificationStatus.PENDING);
    }

    public SellerVerificationEntity approveVerification(UUID verificationId, UserEntity admin) {

        SellerVerificationEntity verification = sellerVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new EntityNotFoundException("Verificação não encontrada"));

        if (!verification.allDocumentsVerified()) {
            throw new IllegalStateException("Documentos ainda não verificados");
        }

        verification.approve(admin);

        UserEntity user = verification.getUser();
        user.setRole(Role.VERIFIED_SELLER);

        userRepository.save(user);
        sellerVerificationRepository.save(verification);

        log.info("Verificação {} aprovada pelo admin {}", verificationId, admin.getEmail());

        return verification;
    }

    public SellerVerificationEntity rejectVerification(UUID verificationId, UserEntity admin, String reason) {

        SellerVerificationEntity verification = sellerVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new EntityNotFoundException("Verificação não encontrada"));

        verification.reject(admin, reason);

        sellerVerificationRepository.save(verification);

        log.info("Verificação {} rejeitada pelo admin {}", verificationId, admin.getEmail());

        return verification;
    }

    public void banVerificationDocuments(UUID verificationId, String reason, UserEntity admin) {

        SellerVerificationEntity verification = sellerVerificationRepository.findById(verificationId)
                .orElseThrow(() -> new EntityNotFoundException("Verificação não encontrada"));

        UserEntity user = verification.getUser();
        UUID bannedUserId = user.getId();

        for (DocumentEntity document : verification.getDocuments()) {
            try {
                blacklistService.banDocument(
                        document.getStoredPath(), // usa o path como valor a hashar
                        document.getDocumentType(),
                        bannedUserId,
                        reason,
                        admin.getName()
                );
            } catch (IllegalStateException e) {
                log.warn("Documento {} já estava banido: {}", document.getStoredPath(), e.getMessage());
            }
        }

        user.setRole(Role.USER);
        userRepository.save(user);

        log.info("Documentos do usuário {} banidos pelo admin {}", user.getId(), admin.getEmail());
    }
}
