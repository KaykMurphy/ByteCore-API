package com.byteCore.demo.service;

import com.byteCore.demo.domain.BannedDocumentEntity;
import com.byteCore.demo.enums.DocumentType;
import com.byteCore.demo.repository.BannedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final DocumentHashService documentHashService;
    private final BannedDocumentRepository bannedDocumentRepository;

    public boolean isDocumentBanned(String rawValue) {
        String hash = documentHashService.generateHash(rawValue);

        return bannedDocumentRepository.existsByDocumentHash(hash);
    }

    public BannedDocumentEntity banDocument(String rawValue,
                                            DocumentType type, UUID bannedUserId,
                                            String reason, String adminEmail){
        String hash = documentHashService.generateHash(rawValue);

        if (bannedDocumentRepository.existsByDocumentHash(hash)) {
            throw new IllegalStateException("Documento já banido");
        }

        BannedDocumentEntity bannedDoc = BannedDocumentEntity.builder()
                .documentHash(hash)
                .documentType(type)
                .bannedUserId(bannedUserId)
                .reason(reason)
                .bannedByAdminEmail(adminEmail)
                .build();

        return bannedDocumentRepository.save(bannedDoc);
    }

    public List<BannedDocumentEntity> getBannedDocumentsByUser(UUID userId) {
        return bannedDocumentRepository.findByBannedUserId(userId);
    }

}

