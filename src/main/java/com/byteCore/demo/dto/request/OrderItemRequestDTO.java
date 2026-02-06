package com.byteCore.demo.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequestDTO {
    private Long productId;
    private Integer quantity;
}