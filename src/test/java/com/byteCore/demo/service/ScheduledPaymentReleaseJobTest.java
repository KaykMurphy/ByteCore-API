package com.byteCore.demo.service;

import com.byteCore.demo.ScheduledPaymentReleaseJob;
import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.domain.OrderItemEntity;
import com.byteCore.demo.domain.PaymentEntity;
import com.byteCore.demo.domain.ProductEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.repository.PaymentRepository;
import com.byteCore.demo.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.TestDataFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduledPaymentReleaseJobTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ScheduledPaymentReleaseJob scheduledPaymentReleaseJob;

    @Captor private ArgumentCaptor<PaymentEntity> paymentCaptor;
    @Captor private ArgumentCaptor<UserEntity> userCaptor;

    @Test
    void releasePayments_whenDataIsValid_shouldUpdateBalancesAndStatus() {

        UserEntity seller = new UserEntity();
        seller.setId(UUID.randomUUID());
        seller.setPendingBalance(new BigDecimal("100.00"));
        seller.setAvailableBalance(new BigDecimal("0.00"));

        OrderEntity mockOrder = new OrderEntity();

        ProductEntity mockProduct = new ProductEntity();
        mockProduct.setPrice(new BigDecimal("100.00"));
        mockProduct.setSeller(seller);

        OrderItemEntity mockItem = new OrderItemEntity();
        mockItem.setPrice(new BigDecimal("100.00"));
        mockItem.setQuantity(1);
        mockItem.setProduct(mockProduct);
        mockOrder.addItem(mockItem);

        PaymentEntity p1 = TestDataFactory.validPaymentEntity();
        p1.setId(1L);
        p1.setSellerAmount(new BigDecimal("40.00"));
        p1.setOrder(mockOrder);

        PaymentEntity p2 = TestDataFactory.validPaymentEntity();
        p2.setId(2L);
        p2.setSellerAmount(new BigDecimal("60.00"));
        p2.setOrder(mockOrder);

        List<PaymentEntity> pagamentosPendentes = Arrays.asList(p1, p2);

        when(paymentRepository.findByStatusAndMoneyReleasedFalseAndMoneyReleaseDateBefore(any(), any()))
                .thenReturn(pagamentosPendentes);

        scheduledPaymentReleaseJob.releasePayments();

        verify(paymentRepository, times(2)).save(paymentCaptor.capture());
        verify(userRepository, times(2)).save(userCaptor.capture());

        List<PaymentEntity> capturedPayments = paymentCaptor.getAllValues();
        Assertions.assertTrue(capturedPayments.get(0).isMoneyReleased());
        Assertions.assertTrue(capturedPayments.get(1).isMoneyReleased());

        UserEntity finalSellerState = userCaptor.getAllValues().get(1);
        Assertions.assertEquals(new BigDecimal("100.00"), finalSellerState.getAvailableBalance());
        Assertions.assertEquals(new BigDecimal("0.00"), finalSellerState.getPendingBalance());
    }

    @Test
    void releasePayments_whenOnePaymentFailsToSave_shouldProcessRemainingPayments(){

        UserEntity seller = new UserEntity();
        seller.setId(UUID.randomUUID());
        seller.setPendingBalance(new BigDecimal("100.00"));
        seller.setAvailableBalance(new BigDecimal("0.00"));

        OrderEntity mockOrder = new OrderEntity();

        ProductEntity mockProduct = new ProductEntity();
        mockProduct.setPrice(new BigDecimal("100.00"));
        mockProduct.setSeller(seller);

        OrderItemEntity mockItem = new OrderItemEntity();
        mockItem.setPrice(new BigDecimal("100.00"));
        mockItem.setQuantity(1);
        mockItem.setProduct(mockProduct);
        mockOrder.addItem(mockItem);

        PaymentEntity p1 = TestDataFactory.validPaymentEntity();
        p1.setId(1L);
        p1.setSellerAmount(new BigDecimal("40.00"));
        p1.setOrder(mockOrder);

        PaymentEntity p2 = TestDataFactory.validPaymentEntity();
        p2.setId(2L);
        p2.setSellerAmount(new BigDecimal("60.00"));
        p2.setOrder(mockOrder);

        List<PaymentEntity> pagamentosPendentes = Arrays.asList(p1, p2);

        when(paymentRepository.findByStatusAndMoneyReleasedFalseAndMoneyReleaseDateBefore(any(), any()))
                .thenReturn(pagamentosPendentes);

        doThrow(new RuntimeException("Simulando queda do banco de dados no P1!"))
                .when(paymentRepository).save(p1);

        Assertions.assertDoesNotThrow(() -> {
            scheduledPaymentReleaseJob.releasePayments();
        });

        verify(paymentRepository, times(2)).save(any(PaymentEntity.class));
        verify(userRepository, times(2)).save(any(UserEntity.class));
    }



}

