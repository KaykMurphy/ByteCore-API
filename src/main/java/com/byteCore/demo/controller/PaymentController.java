package com.byteCore.demo.controller;

import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.response.PixPaymentResponseDTO;
import com.byteCore.demo.security.CustomUserDetails;
import com.byteCore.demo.service.PixPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PixPaymentService pixPaymentService;

    @PostMapping("/pix/{orderId}")
    public ResponseEntity<PixPaymentResponseDTO> createPayment(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // utilizador logado
        UserEntity user = userDetails.getUser();

        PixPaymentResponseDTO response = pixPaymentService.createPixPayment(user, orderId);

        return ResponseEntity.ok(response);
    }
}