package com.byteCore.demo.service;

import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.domain.WithdrawalRequestEntity;
import com.byteCore.demo.enums.WithdrawalStatus;
import com.byteCore.demo.repository.WithdrawalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final WithdrawalRequestRepository withdrawalRepository;
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("500.00");

    @Transactional
    public WithdrawalRequestEntity requestWithdrawal(
            UserEntity seller,
            BigDecimal amount,
            String pixKey) {

        if (!seller.canWithdraw(amount)) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        BigDecimal totalToday = withdrawalRepository.getTotalWithdrawnToday(
                seller.getId(),
                startOfDay
        );

        if (totalToday.add(amount).compareTo(DAILY_LIMIT) > 0) {
            throw new IllegalStateException(
                    String.format("Limite diário excedido. Já sacou: R$ %s hoje. Limite: R$ 500",
                            totalToday)
            );
        }

        WithdrawalRequestEntity withdrawal = WithdrawalRequestEntity.builder()
                .seller(seller)
                .amount(amount)
                .pixKey(pixKey)
                .status(WithdrawalStatus.PENDING)
                .build();

        return withdrawalRepository.save(withdrawal);
    }
}