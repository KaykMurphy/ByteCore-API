package com.byteCore.demo.controller;

import com.byteCore.demo.domain.User;
import com.byteCore.demo.dto.response.PixPaymentResponseDTO;
import com.byteCore.demo.security.CustomUserDetails;
import com.byteCore.demo.service.PixPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PixPaymentService pixPaymentService;

    @PostMapping("/pix/{orderId}")
    public ResponseEntity<PixPaymentResponseDTO> createPayment(
            @PathVariable Long orderId,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // utilizador logado
        User user = userDetails.getUser();

        PixPaymentResponseDTO response = pixPaymentService.createPixPayment(user, amount, orderId);

        return ResponseEntity.ok(response);
    }
}