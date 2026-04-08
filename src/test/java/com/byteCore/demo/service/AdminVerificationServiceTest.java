package com.byteCore.demo.service;

import com.byteCore.demo.domain.DocumentEntity;
import com.byteCore.demo.domain.SellerVerificationEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.enums.DocumentType;
import com.byteCore.demo.enums.Role;
import com.byteCore.demo.enums.VerificationStatus;
import com.byteCore.demo.repository.SellerVerificationRepository;
import com.byteCore.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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

        ArgumentCaptor<UserEntity> userCaptor
                = ArgumentCaptor.forClass(UserEntity.class);

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

    @Test
    void approveVerification_shouldThrowException_whenVerificationDoesNotExist() {

        UUID verificationId = UUID.randomUUID();
        UserEntity admin = TestDataFactory.validAdmin();

        when(sellerVerificationRepository.findById(verificationId))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> adminVerificationService.approveVerification(verificationId, admin)
        );


        Assertions.assertEquals(
                "Verificação não encontrada",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(UserEntity.class));

        verify(sellerVerificationRepository, times(1))
                .findById(verificationId);

        verify(sellerVerificationRepository, never())
                .save(any(SellerVerificationEntity.class));
    }

    @Test
    void approveVerification_shouldThrowException_whenDocumentsNotVerified() {

        UUID verificationId = UUID.randomUUID();
        UserEntity admin = TestDataFactory.validAdmin();
        UserEntity user = TestDataFactory.validUser();

        SellerVerificationEntity verification
                = TestDataFactory.validVerificationEntity(user);

        when(sellerVerificationRepository.findById(verificationId))
                .thenReturn(Optional.of(verification));

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> adminVerificationService.approveVerification(verificationId, admin)
        );


        Assertions.assertEquals(
                "Documentos ainda não verificados",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(UserEntity.class));

        verify(sellerVerificationRepository, times(1))
                .findById(verificationId);

        verify(sellerVerificationRepository, never())
                .save(any(SellerVerificationEntity.class));
    }

    @Test
    void rejectVerification_shouldRejectVerification_whenDataIsValid() {

        UUID verificationId =  UUID.randomUUID();
        UserEntity admin = TestDataFactory.validAdmin();
        UserEntity user =  TestDataFactory.validUser();
        String reason = "Documento ilegível";

        SellerVerificationEntity verification
                = TestDataFactory.validVerificationEntity(user);

        when(sellerVerificationRepository.findById(verificationId))
                .thenReturn(Optional.of(verification));

        SellerVerificationEntity result =
                adminVerificationService.rejectVerification(
                        verificationId, admin, reason);

        ArgumentCaptor<SellerVerificationEntity> verificationCaptor
                =  ArgumentCaptor.forClass(SellerVerificationEntity.class);

        verify(sellerVerificationRepository, times(1))
                .save(verificationCaptor.capture());

        SellerVerificationEntity capturedVerification = verificationCaptor.getValue();

        Assertions.assertEquals(VerificationStatus.REJECTED, capturedVerification.getStatus());
        Assertions.assertEquals(reason, capturedVerification.getRejectionReason());

        verify(sellerVerificationRepository, times(1))
                .findById(verificationId);

        verifyNoInteractions(userRepository);
    }

    @Test
    void rejectVerification_shouldThrowException_whenVerificationDoesNotExist() {
        UUID verificationId =  UUID.randomUUID();
        UserEntity admin = TestDataFactory.validAdmin();
        String reason = "Documento ilegível";

        when(sellerVerificationRepository.findById(verificationId))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> adminVerificationService.rejectVerification(
                        verificationId, admin, reason
                )
        );

        Assertions.assertEquals(
                "Verificação não encontrada",
                exception.getMessage()
        );

        verify(sellerVerificationRepository, never())
                .save(any(SellerVerificationEntity.class));

        verify(sellerVerificationRepository, times(1))
                .findById(verificationId);
    }

    @Test
    void banVerificationDocuments_shouldBanSuccessfully_whenDataIsValid() {

        UUID verificationId =  UUID.randomUUID();
        UserEntity admin = TestDataFactory.validAdmin();
        UserEntity user = TestDataFactory.validUser();
        String reason = "Documento negado";

        SellerVerificationEntity verification =
                TestDataFactory.validVerificationEntity(user);

        DocumentEntity document = new DocumentEntity();
        document.setStoredPath("caminho/do/documento.pdf");
        document.setDocumentType(DocumentType.RG);
        verification.addDocument(document);

        when(sellerVerificationRepository.findById(verificationId))
                .thenReturn(Optional.of(verification));

        adminVerificationService.banVerificationDocuments(
                verificationId, reason, admin);

        verify(blacklistService, times(1)).banDocument(
                eq("caminho/do/documento.pdf"),
                eq(DocumentType.RG),
                eq(user.getId()),
                eq(reason),
                eq(admin.getEmail())
        );

        ArgumentCaptor<UserEntity> userCaptor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(userRepository, times(1))
                .save(userCaptor.capture());

        UserEntity capturedUser = userCaptor.getValue();
        Assertions.assertEquals(user, capturedUser);
        Assertions.assertEquals(Role.USER, capturedUser.getRole());

        verify(sellerVerificationRepository, never()).save(any(SellerVerificationEntity.class));
        verify(sellerVerificationRepository, times(1)).findById(verificationId);
    }

    @Test
    void banVerificationDocuments_shouldThrowException_whenVerificationDoesNotExist() {

        UUID verificationId =  UUID.randomUUID();
        UserEntity admin = TestDataFactory.validAdmin();
        String reason =  "Documento negado";


        when(sellerVerificationRepository.findById(verificationId))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> adminVerificationService.banVerificationDocuments(
                        verificationId, reason, admin)
        );

        Assertions.assertEquals(
                "Verificação não encontrada",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(UserEntity.class));

        verifyNoInteractions(blacklistService);

        verify(sellerVerificationRepository, times(1))
                .findById(verificationId);
    }

    @Test
    void banVerificationDocuments_shouldContinueAndDemoteUser_whenDocumentIsAlreadyBanned() {

        UUID verificationId =  UUID.randomUUID();
        UserEntity admin = TestDataFactory.validAdmin();
        UserEntity user = TestDataFactory.validUser();

        String reason = "Documento negado";

        SellerVerificationEntity verification = TestDataFactory.validVerificationEntity(
                user
        );

        DocumentEntity document = TestDataFactory.validDocumentEntity();
        verification.addDocument(document);

        when(sellerVerificationRepository.findById(verificationId))
                .thenReturn(Optional.of(verification));


        doThrow(new IllegalStateException(reason))
                .when(blacklistService).banDocument(
                        any(), any(), any(), any(), any()
                );

        Assertions.assertDoesNotThrow(() ->
                adminVerificationService.banVerificationDocuments(verificationId, reason, admin)
        );

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        UserEntity capturedUser = userCaptor.getValue();
        Assertions.assertEquals(user, capturedUser);
        Assertions.assertEquals(Role.USER, capturedUser.getRole());

        verify(sellerVerificationRepository, never()).save(any(SellerVerificationEntity.class));
























    }












}

