package com.byteCore.demo.controller;

import com.byteCore.demo.domain.SellerVerificationEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.SellerVerificationMapper;
import com.byteCore.demo.dto.request.SellerVerificationRequestDTO;
import com.byteCore.demo.dto.response.SellerVerificationResponseDTO;
import com.byteCore.demo.repository.SellerVerificationRepository;
import com.byteCore.demo.repository.UserRepository;
import com.byteCore.demo.security.CustomUserDetails;
import com.byteCore.demo.SellerVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/verifications")
public class SellerVerificationController {


    private final SellerVerificationRepository sellerVerificationRepository;
    private final SellerVerificationMapper sellerVerificationMapper;
    private final UserRepository userRepository;
    private final SellerVerificationService sellerVerificationService;


    @PostMapping()
    public ResponseEntity<SellerVerificationResponseDTO> submitVerification(
            @Valid @RequestBody SellerVerificationRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails){

        UserEntity user = userDetails.getUser();


        SellerVerificationEntity savedEntity
                = sellerVerificationService.submitVerification(user, dto);

        SellerVerificationResponseDTO response =
                sellerVerificationMapper.toResponseDTO(savedEntity);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<SellerVerificationResponseDTO>> getMyVerificationHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserEntity user = userDetails.getUser();

        List<SellerVerificationEntity> verifications = sellerVerificationService.getMyVerificationHistory(user.getId());

        List<SellerVerificationResponseDTO> response =
                verifications.stream()
                .map(sellerVerificationMapper::toResponseDTO)
                .toList();

        return ResponseEntity.ok(response);
    }

}
