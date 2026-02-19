package com.byteCore.demo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderCreateDTO {
    @Valid
    @NotEmpty
    private List<OrderItemRequestDTO> items;
}