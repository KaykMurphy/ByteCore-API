package com.byteCore.demo.controller;

import com.byteCore.demo.domain.SellerVerificationEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.SellerVerificationMapper;
import com.byteCore.demo.dto.response.SellerVerificationResponseDTO;
import com.byteCore.demo.security.CustomUserDetails;
import com.byteCore.demo.AdminVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/verifications")
public class AdminVerificationController {

    private final AdminVerificationService adminVerificationService;
    private final SellerVerificationMapper sellerVerificationMapper;

    @GetMapping("/pending")
    public ResponseEntity<List<SellerVerificationResponseDTO>> getPendingVerifications() {

        List<SellerVerificationEntity> list = adminVerificationService.listPendingVerifications();

        List<SellerVerificationResponseDTO> dtoList = list.stream()
                .map(sellerVerificationMapper::toResponseDTO)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<SellerVerificationResponseDTO> approveVerification(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserEntity admin = userDetails.getUser();

        SellerVerificationEntity entity = adminVerificationService.approveVerification(id, admin);

        SellerVerificationResponseDTO response = sellerVerificationMapper.toResponseDTO(entity);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<SellerVerificationResponseDTO> rejectVerification(
            @PathVariable UUID id, @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails){

        UserEntity admin =  userDetails.getUser();

        SellerVerificationEntity entity = adminVerificationService.rejectVerification(id, admin, reason);

        SellerVerificationResponseDTO response = sellerVerificationMapper.toResponseDTO(entity);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/ban")
    public ResponseEntity<SellerVerificationResponseDTO> banVerificationDocuments(
            @PathVariable UUID id, @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails){

        UserEntity admin = userDetails.getUser();

        adminVerificationService.banVerificationDocuments(id, reason, admin);

        return ResponseEntity.noContent().build();
    }
}