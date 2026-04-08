package com.byteCore.demo.service;

import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.domain.WithdrawalRequestEntity;
import com.byteCore.demo.enums.WithdrawalStatus;
import com.byteCore.demo.repository.WithdrawalRequestRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WithdrawalServiceTest {

    @Mock
    private WithdrawalRequestRepository withdrawalRepository;

    @InjectMocks
    private WithdrawalService withdrawalService;

    @Test
    void requestWithdrawal_shouldCreateWithdrawalSuccessfully() {

        UserEntity seller = new UserEntity();
        seller.setId(UUID.randomUUID());
        seller.setAvailableBalance(new BigDecimal("1000.00"));

        String pixKey = "111.111.111-11";
        BigDecimal amount = new BigDecimal("100.00");

        WithdrawalRequestEntity mockResponse = new WithdrawalRequestEntity();
        mockResponse.setStatus(WithdrawalStatus.PENDING);

        when(withdrawalRepository.getTotalWithdrawnToday(
                eq(seller.getId()),
                any(Instant.class)
        )).thenReturn(BigDecimal.ZERO);

        when(withdrawalRepository.save(any(WithdrawalRequestEntity.class)))
                .thenReturn(mockResponse);

        WithdrawalRequestEntity result =
                withdrawalService.requestWithdrawal(seller, amount, pixKey);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(WithdrawalStatus.PENDING, result.getStatus());

        // objeto salvo
        ArgumentCaptor<WithdrawalRequestEntity> captor =
                ArgumentCaptor.forClass(WithdrawalRequestEntity.class);

        verify(withdrawalRepository, times(1))
                .save(captor.capture());

        WithdrawalRequestEntity saved = captor.getValue();

        Assertions.assertEquals(seller, saved.getSeller());
        Assertions.assertEquals(amount, saved.getAmount());
        Assertions.assertEquals(pixKey, saved.getPixKey());
        Assertions.assertEquals(WithdrawalStatus.PENDING, saved.getStatus());
    }

    @Test
    void requestWithdrawal_shouldThrowException_whenInsufficientBalance() {

        UserEntity seller = new UserEntity();
        seller.setId(UUID.randomUUID());
        seller.setAvailableBalance(new BigDecimal("200.00"));

        BigDecimal amount = new BigDecimal("1000.00");
        String pixKey = "111.111.111-11";

        IllegalStateException exception =
                Assertions.assertThrows(IllegalStateException.class,
                        () -> withdrawalService.requestWithdrawal(
                                seller, amount, pixKey));

        Assertions.assertEquals("Saldo insuficiente", exception.getMessage());

        verify(withdrawalRepository, never())
                .save(any(WithdrawalRequestEntity.class));
    }

    @Test
    void requestWithdrawal_shouldThrowException_whenDailyLimitExceeded() {

        UserEntity seller = new UserEntity();
        seller.setId(UUID.randomUUID());
        seller.setAvailableBalance(new BigDecimal("2100.00"));

        BigDecimal amount = new BigDecimal("1000.00");
        String pixKey = "111.111.111-11";

        when(withdrawalRepository.getTotalWithdrawnToday(
                eq(seller.getId()),
                any(Instant.class)
        )).thenReturn(BigDecimal.ZERO);

        IllegalStateException exception =
                Assertions.assertThrows(IllegalStateException.class,
                        () -> withdrawalService.requestWithdrawal(
                                seller, amount, pixKey
                        ));

        Assertions.assertEquals(
                "Limite diário excedido. Já sacou: R$ 0 hoje. Limite: R$ 500",
                exception.getMessage()
        );

        verify(withdrawalRepository, never())
                .save(any(WithdrawalRequestEntity.class));
    }
}