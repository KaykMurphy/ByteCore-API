package com.byteCore.demo.service;

import com.byteCore.demo.domain.SellerVerificationEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.SellerVerificationMapper;
import com.byteCore.demo.dto.request.SellerVerificationRequestDTO;
import com.byteCore.demo.enums.VerificationStatus;
import com.byteCore.demo.repository.SellerVerificationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.TestDataFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerVerificationServiceTest {

    @Mock
    private SellerVerificationRepository sellerVerificationRepository;

    @Mock
    private SellerVerificationMapper sellerVerificationMapper;

    @InjectMocks
    private SellerVerificationService sellerVerificationService;


    @Test
    void submitVerification_shouldSubmitSuccessfully_whenDataIsValid() {

        UserEntity user = TestDataFactory.validUser();

        SellerVerificationRequestDTO dto
                = TestDataFactory.validSellerVerificationRequestDTO();

        SellerVerificationEntity mappedEntity
                = TestDataFactory.validVerificationEntity(user);

        when(sellerVerificationRepository.existsByUserIdAndStatus(
                user.getId(), VerificationStatus.PENDING))
                .thenReturn(false);

        when(sellerVerificationMapper.toEntity(dto))
                .thenReturn(mappedEntity);

        when(sellerVerificationRepository.save(any(SellerVerificationEntity.class)))
                .thenReturn(mappedEntity);

        SellerVerificationEntity result =
                sellerVerificationService.submitVerification(
                        user, dto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(VerificationStatus.PENDING, result.getStatus());
        Assertions.assertEquals(user, result.getUser());


        ArgumentCaptor<SellerVerificationEntity>  captor = ArgumentCaptor.forClass(SellerVerificationEntity.class);
        verify(sellerVerificationRepository, times(1))
                .save(captor.capture());

        SellerVerificationEntity capturedEntity = captor.getValue();

        Assertions.assertNotNull(capturedEntity);
        Assertions.assertEquals(user, capturedEntity.getUser());
        Assertions.assertEquals(VerificationStatus.PENDING, capturedEntity.getStatus());

        verify(sellerVerificationRepository, times(1))
                .existsByUserIdAndStatus(user.getId(), VerificationStatus.PENDING);

        verify(sellerVerificationMapper, times(1))
                .toEntity(dto);
    }


    @Test
    void submitVerification_shouldThrowException_whenUserHasPendingVerification() {

        UserEntity user = TestDataFactory.validUser();

        SellerVerificationRequestDTO dto
                =  TestDataFactory.validSellerVerificationRequestDTO();

        when(sellerVerificationRepository.existsByUserIdAndStatus(user.getId(), VerificationStatus.PENDING))
                .thenReturn(true);

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> sellerVerificationService.submitVerification(user, dto)
        );

        Assertions.assertNotNull(exception);

        Assertions.assertEquals(
                "Você já possui uma verificação em análise.",
                exception.getMessage()
        );

        verify(sellerVerificationRepository, times(1))
                .existsByUserIdAndStatus(user.getId(), VerificationStatus.PENDING);


        verify(sellerVerificationRepository, never())
                .save(any(SellerVerificationEntity.class));

        verifyNoInteractions(sellerVerificationMapper);
    }

    @Test
    void getMyVerificationHistory_shouldReturnHistory_whenDataIsValid() {

        UserEntity user = TestDataFactory.validUser();

        SellerVerificationEntity entity
                = TestDataFactory.validVerificationEntity(user);

        List<SellerVerificationEntity> list = List.of(entity);

        when(sellerVerificationRepository.findByUserIdOrderBySubmittedAtDesc(user.getId()))
                .thenReturn(list);

        List<SellerVerificationEntity> result  = sellerVerificationService.getMyVerificationHistory(
                user.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(list, result);
        Assertions.assertEquals(1, result.size());


        verify(sellerVerificationRepository, times(1))
                .findByUserIdOrderBySubmittedAtDesc(user.getId());
    }



}