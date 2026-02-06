package com.byteCore.demo.controller;

import com.byteCore.demo.domain.Order;
import com.byteCore.demo.dto.request.OrderCreateDTO;
import com.byteCore.demo.security.CustomUserDetails;
import com.byteCore.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody OrderCreateDTO dto,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Order order = orderService.createOrder(userDetails.getUser(), dto);
        return ResponseEntity.ok(order);
    }
}