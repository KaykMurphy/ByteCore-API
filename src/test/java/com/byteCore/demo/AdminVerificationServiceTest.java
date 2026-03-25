package com.byteCore.demo;

import com.byteCore.demo.domain.SellerVerificationEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.enums.Role;
import com.byteCore.demo.enums.VerificationStatus;
import com.byteCore.demo.repository.SellerVerificationRepository;
import com.byteCore.demo.repository.UserRepository;
import com.byteCore.demo.service.AdminVerificationService;
import com.byteCore.demo.service.BlacklistService;
import com.byteCore.demo.service.SellerVerificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.TestDataFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminVerificationServiceTest {

    @Mock
    private SellerVerificationRepository sellerVerificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BlacklistService blacklistService;

    @InjectMocks
    private AdminVerificationService adminVerificationService;


    @Test
    void listPendingVerifications_shouldReturnPendingList_whenDataIsValid() {

        UserEntity user = TestDataFactory.validUser();

        SellerVerificationEntity entity =
                TestDataFactory.validVerificationEntity(user);

        List<SellerVerificationEntity> list = List.of(entity);

        when(sellerVerificationRepository.findByStatusOrderBySubmittedAtAsc(
                VerificationStatus.PENDING
        )).thenReturn(list);


        List<SellerVerificationEntity> result =
                adminVerificationService.listPendingVerifications();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(list, result);
        Assertions.assertEquals(1, result.size());

        verify(sellerVerificationRepository, times(1))
                .findByStatusOrderBySubmittedAtAsc(VerificationStatus.PENDING);
    }

    @Test
    void approveVerification_shouldApproveVerification_whenDataIsValid() {

        UUID verificationId = UUID.randomUUID();
        UserEntity admin = TestDataFactory.validAdmin();
        UserEntity user = TestDataFactory.validUser();

        SellerVerificationEntity realEntity
                = TestDataFactory.validVerificationEntity(user);

        SellerVerificationEntity verificationSpy = spy(realEntity);

        doReturn(true).when(verificationSpy).allDocumentsVerified();

        when(sellerVerificationRepository.findById(verificationId))
                .thenReturn(Optional.of(verificationSpy));

        SellerVerificationEntity result =
                adminVerificationService.approveVerification(
                        verificationId, admin);

        Assertions.assertNotNull(result);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(1))
                .save(userCaptor.capture());

        UserEntity capturedUser = userCaptor.getValue();
        Assertions.assertEquals(user, capturedUser);
        Assertions.assertEquals(Role.VERIFIED_SELLER, capturedUser.getRole());

        ArgumentCaptor<SellerVerificationEntity> verificationCaptor
                = ArgumentCaptor.forClass(SellerVerificationEntity.class);

        verify(sellerVerificationRepository, times(1))
                .save(verificationCaptor.capture());

        SellerVerificationEntity capturedVerification
                = verificationCaptor.getValue();

        Assertions.assertEquals(
                VerificationStatus.APPROVED, capturedVerification.getStatus()); // Garante que foi aprovado!

        verify(sellerVerificationRepository, times(1))
                .findById(verificationId);
    }



}
