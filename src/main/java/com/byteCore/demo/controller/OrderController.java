package com.byteCore.demo.controller;

import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.dto.mapper.OrderMapper;
import com.byteCore.demo.dto.request.OrderCreateDTO;
import com.byteCore.demo.dto.response.OrderResponseDTO;
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
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@RequestBody OrderCreateDTO dto,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderEntity order = orderService.createOrder(userDetails.getUser(), dto);

        OrderResponseDTO response = orderMapper.toResponseDTO(order);

        return ResponseEntity.ok(response);
    }
}