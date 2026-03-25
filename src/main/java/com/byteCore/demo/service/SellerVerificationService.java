package com.byteCore.demo.service;

import com.byteCore.demo.domain.SellerVerificationEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.SellerVerificationMapper;
import com.byteCore.demo.dto.request.SellerVerificationRequestDTO;
import com.byteCore.demo.enums.VerificationStatus;
import com.byteCore.demo.repository.SellerVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerVerificationService {

    private final SellerVerificationRepository sellerVerificationRepository;
    private final SellerVerificationMapper sellerVerificationMapper;

    @Transactional
    public SellerVerificationEntity submitVerification(UserEntity user, SellerVerificationRequestDTO dto) {

        log.info("Usuário {} está submetendo uma nova verificação de vendedor",
                user.getEmail());

        boolean hasPending = sellerVerificationRepository.existsByUserIdAndStatus(
                user.getId(), VerificationStatus.PENDING);

        if (hasPending) {
            log.warn("Usuário {} tentou submeter verificação, mas já possui uma PENDING.",
                    user.getId());
            throw new IllegalStateException("Você já possui uma verificação em análise.");
        }

        SellerVerificationEntity entity = sellerVerificationMapper.toEntity(dto);

        entity.setUser(user);
        entity.setStatus(VerificationStatus.PENDING);

        return sellerVerificationRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<SellerVerificationEntity> getMyVerificationHistory(UUID userId) {
        log.info("Buscando histórico de verificações para o usuário ID: {}", userId);

        return sellerVerificationRepository.findByUserIdOrderBySubmittedAtDesc(userId);
    }
}