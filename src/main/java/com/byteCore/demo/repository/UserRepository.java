package com.byteCore.demo.repository;

import com.byteCore.demo.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    @Modifying
    @Query("UPDATE UserEntity u SET u.availableBalance = u.availableBalance - :amount " +
            "WHERE u.id = :userId AND u.availableBalance >= :amount")
    int deductBalanceAtomic(@Param("userId") UUID userId,
                            @Param("amount") BigDecimal amount);
}
