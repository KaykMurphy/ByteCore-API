package com.byteCore.demo.service;

import com.byteCore.demo.PixPaymentService;
import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.domain.PaymentEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.PaymentMapper;
import com.byteCore.demo.dto.response.PixPaymentResponseDTO;
import com.byteCore.demo.enums.OrderStatus;
import com.byteCore.demo.enums.PaymentMethod;
import com.byteCore.demo.enums.PaymentStatus;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.PaymentRepository;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentPointOfInteraction;
import com.mercadopago.resources.payment.PaymentTransactionData;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PixPaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private PixPaymentService pixPaymentService;

    @Test
    void createPixPayment_shouldCreatePaymentSuccessfully() throws Exception {

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail("email.teste@gmail.com");
        user.setName("teste");

        Long orderId = 1L;

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setTotalAmount(new BigDecimal("100.00"));

        OffsetDateTime createdAt = OffsetDateTime.now();
        OffsetDateTime expiresAt = createdAt.plusMinutes(30);

        Payment mpPayment = mock(Payment.class);
        when(mpPayment.getId()).thenReturn(123456789L);
        when(mpPayment.getDateCreated()).thenReturn(createdAt);
        when(mpPayment.getTransactionAmount()).thenReturn(new BigDecimal("100.00"));
        when(mpPayment.getDateOfExpiration()).thenReturn(expiresAt);

        PaymentPointOfInteraction poi = mock(PaymentPointOfInteraction.class);
        PaymentTransactionData data = mock(PaymentTransactionData.class);

        when(data.getQrCodeBase64()).thenReturn("base64-falso-do-qr-code");
        when(data.getQrCode()).thenReturn("pix-copia-e-cola-falso");

        when(poi.getTransactionData()).thenReturn(data);
        when(mpPayment.getPointOfInteraction()).thenReturn(poi);

        PixPaymentResponseDTO expectedResponse = new PixPaymentResponseDTO(
                100L,
                "123456789",
                new BigDecimal("100.00"),
                "PENDING",
                "base64-falso-do-qr-code",
                "pix-copia-e-cola-falso",
                expiresAt.toInstant(),
                orderId
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentClient.create(any())).thenReturn(mpPayment);

        when(paymentMapper.toDto(
                any(PaymentEntity.class),
                anyString(),
                anyString(),
                eq(orderId)
        )).thenReturn(expectedResponse);

        PixPaymentResponseDTO result = pixPaymentService.createPixPayment(user, orderId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedResponse, result);

        verify(orderRepository, times(1)).findById(orderId);
        verify(paymentClient, times(1)).create(any());
        verify(paymentRepository, times(1)).save(any(PaymentEntity.class));
        verify(paymentMapper, times(1)).toDto(
                any(PaymentEntity.class),
                eq("base64-falso-do-qr-code"),
                eq("pix-copia-e-cola-falso"),
                eq(orderId)
        );

        ArgumentCaptor<PaymentEntity> captor =
                ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository).save(captor.capture());

        PaymentEntity savedPayment = captor.getValue();

        Assertions.assertEquals("123456789", savedPayment.getExternalId());
        Assertions.assertEquals(new BigDecimal("100.00"), savedPayment.getAmount());
        Assertions.assertEquals(PaymentStatus.PENDING, savedPayment.getStatus());
        Assertions.assertEquals(PaymentMethod.PIX, savedPayment.getMethod());
        Assertions.assertEquals(user, savedPayment.getUser());
        Assertions.assertEquals(order, savedPayment.getOrder());
        Assertions.assertEquals("pix-copia-e-cola-falso", savedPayment.getQrCode());
        Assertions.assertEquals(createdAt.toInstant(), savedPayment.getCreatedAt());
        Assertions.assertEquals(expiresAt.toInstant(), savedPayment.getExpiresAt());
    }


    @Test
    void createPixPayment_shouldThrowException_whenOrderNotFound() throws Exception {

        UserEntity user = new  UserEntity();
        user.setId(UUID.randomUUID());

        Long orderId = 1L;

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception =
                Assertions.assertThrows(
                        EntityNotFoundException.class,
                        () -> pixPaymentService.createPixPayment(user, orderId)
                );

        Assertions.assertEquals(
                "Pedido não encontrado",
                exception.getMessage()
        );

        verify(orderRepository, times(1))
                .findById(orderId);

        verify(paymentRepository, never())
                .save(any(PaymentEntity.class));

        verify(paymentClient, never())
                .create(any());
    }


    @Test
    void createPixPayment_shouldThrowException_whenOrderBelongsToAnotherUser() throws Exception  {
        UserEntity invaderUser = new UserEntity();
        invaderUser.setId(UUID.randomUUID());

        UserEntity realOwner = new UserEntity();
        realOwner.setId(UUID.randomUUID());

        Long orderId = 1L;

        OrderEntity order =  new OrderEntity();
        order.setId(orderId);
        order.setUser(realOwner);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));


        IllegalStateException exception =
                Assertions.assertThrows(
                        IllegalStateException.class,
                        () -> pixPaymentService.createPixPayment(invaderUser, orderId)
                );

        Assertions.assertEquals(
                "Pedido não pertence a este usuário",
                exception.getMessage()
        );


        verify(orderRepository, times(1))
                .findById(orderId);

        verify(paymentRepository, never())
                .save(any());

        verify(paymentClient, never())
                .create(any());
    }

    @Test
    void createPixPayment_shouldThrowException_whenOrderStatusIsDifferentFromPendingPayment() throws Exception {

        UserEntity user = new  UserEntity();
        user.setId(UUID.randomUUID());

        Long orderId = 1L;

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        IllegalStateException exception =
                Assertions.assertThrows(
                        IllegalStateException.class,
                        () -> pixPaymentService.createPixPayment(user, orderId));

        Assertions.assertEquals(
                "Pedido não está aguardando pagamento",
                exception.getMessage()
        );

        verify(orderRepository, times(1))
                .findById(orderId);

        verify(paymentRepository, never())
                .save(any());

        verify(paymentClient, never())
                .create(any());
    }

    @Test
    void createPixPayment_shouldThrowException_whenOrderAlreadyHasPayment() throws Exception {

        UserEntity user = new  UserEntity();
        user.setId(UUID.randomUUID());

        Long orderId = 1L;

        PaymentEntity payment = new  PaymentEntity();
        payment.setId(orderId);
        payment.setUser(user);

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setPayment(payment);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        IllegalStateException exception =
                Assertions.assertThrows(
                        IllegalStateException.class,
                        () -> pixPaymentService.createPixPayment(user, orderId)
                );

        Assertions.assertEquals(
                "Pedido já possui pagamento",
                exception.getMessage()
        );

        verify(orderRepository, times(1))
                .findById(orderId);

        verify(paymentRepository, never())
                .save(any(PaymentEntity.class));

        verify(paymentClient, never())
                .create(any());

    }

    @Test
    void createPixPayment_shouldThrowRuntimeException_whenMercadoPagoConnectionFails() throws Exception {

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        Long orderId = 1L;
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setTotalAmount(new BigDecimal("100.00"));

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentClient.create(any()))
                .thenThrow(new MPException("Conexão com Mercado Pago falhou"));

        RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                () -> pixPaymentService.createPixPayment(user, orderId)
        );

        Assertions.assertEquals("Erro interno de pagamento"
                , exception.getMessage());

        verify(paymentRepository, never()).save(any());
    }


    @Test
    void getPaymentStatus_shouldReturnPayment_whenPaymentExists() throws MPException, MPApiException {

        Long paymentId = 1L;

        PaymentEntity localPayment = new PaymentEntity();
        localPayment.setId(paymentId);
        localPayment.setStatus(PaymentStatus.APPROVED);
        localPayment.setQrCode("pix-copia-e-cola-falso");

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(localPayment));

        PixPaymentResponseDTO expectedResponse = new PixPaymentResponseDTO(
                paymentId,
                "ext-123",
                new BigDecimal("100.00"),
                "APPROVED",
                null, // O base64 é null no getPaymentStatus
                "pix-copia-e-cola-falso",
                Instant.now().plus(30, ChronoUnit.MINUTES),
                1L
        );

        when(paymentMapper.toDto(
                localPayment,
                null,
                "pix-copia-e-cola-falso",
                null
        )).thenReturn(expectedResponse);

        PixPaymentResponseDTO result = pixPaymentService.getPaymentStatus(paymentId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedResponse, result);

        verify(paymentClient, never()).get(anyLong());
    }
}





















