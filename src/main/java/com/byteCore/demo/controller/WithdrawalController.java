package com.byteCore.demo.controller;

import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.domain.WithdrawalRequestEntity;
import com.byteCore.demo.dto.mapper.WithdrawalMapper;
import com.byteCore.demo.dto.request.WithdrawalRequestDTO;
import com.byteCore.demo.dto.response.WithdrawalResponseDTO;
import com.byteCore.demo.repository.WithdrawalRequestRepository;
import com.byteCore.demo.security.CustomUserDetails;
import com.byteCore.demo.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

    private final WithdrawalService  withdrawalService;
    private final WithdrawalMapper withdrawalMapper;
    private final WithdrawalRequestRepository withdrawalRepository;

    @PostMapping
    public ResponseEntity<WithdrawalResponseDTO> requestWithdrawal(
            @Valid @RequestBody WithdrawalRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails){

        UserEntity seller = userDetails.getUser();

        WithdrawalRequestEntity entity = withdrawalService.requestWithdrawal(
                seller,
                dto.amount(),
                dto.pixKey());


        WithdrawalResponseDTO response = withdrawalMapper.toResponseDTO(entity);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<WithdrawalResponseDTO>> getMyWithdrawals(
            @AuthenticationPrincipal CustomUserDetails userDetails){

        UserEntity user = userDetails.getUser();

        List<WithdrawalRequestEntity> list = withdrawalRepository.
                findBySellerIdOrderByRequestedAtDesc(user.getId());

        List<WithdrawalResponseDTO> dtoList =
                withdrawalMapper.toResponseDTOList(list);

        return ResponseEntity.ok(dtoList);

    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, BigDecimal>> getCurrentBalance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserEntity seller = userDetails.getUser();

        return ResponseEntity.ok(Map.of(
                "availableBalance", seller.getAvailableBalance(),
                "pendingBalance", seller.getPendingBalance()
        ));
    }

















}
