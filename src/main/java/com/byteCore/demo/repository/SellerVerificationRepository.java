package com.byteCore.demo.repository;

import com.byteCore.demo.domain.SellerVerificationEntity;
import com.byteCore.demo.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SellerVerificationRepository extends JpaRepository<SellerVerificationEntity, UUID> {

    // buscar todas as verificações de um usuário, ordenadas por data
    List<SellerVerificationEntity> findByUserIdOrderBySubmittedAtDesc(UUID userId);

    // buscar verificação mais recente de um user
    Optional<SellerVerificationEntity> findFirstByUserIdOrderBySubmittedAtDesc(UUID userId);

    // buscar todas verificações por status (pra admin listar)
    List<SellerVerificationEntity> findByStatusOrderBySubmittedAtAsc(VerificationStatus status);

    boolean existsByUserIdAndStatus(UUID userId, VerificationStatus status);

}

