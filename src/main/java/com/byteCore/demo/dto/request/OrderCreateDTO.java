package com.byteCore.demo.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderCreateDTO {
    private List<OrderItemRequestDTO> items;
}