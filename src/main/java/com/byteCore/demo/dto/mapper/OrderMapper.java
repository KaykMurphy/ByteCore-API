package com.byteCore.demo.dto.mapper;

import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.domain.OrderItemEntity;
import com.byteCore.demo.dto.response.OrderItemResponseDTO;
import com.byteCore.demo.dto.response.OrderResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {


    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "items", source = "items")
    OrderResponseDTO toResponseDTO(OrderEntity order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productTitle", source = "product.title")
    OrderItemResponseDTO toItemDTO(OrderItemEntity  item);

}
