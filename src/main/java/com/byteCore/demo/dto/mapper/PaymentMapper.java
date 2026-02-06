package com.byteCore.demo.dto.mapper;

import com.byteCore.demo.domain.PaymentEntity;
import com.byteCore.demo.dto.response.PixPaymentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "paymentId", source = "entity.id")
    @Mapping(target = "pixQrCode", source = "qrCodeBase64")
    @Mapping(target = "pixQrCodeText", source = "qrCodeText")
    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    PixPaymentResponseDTO toDto(PaymentEntity entity, String qrCodeBase64, String qrCodeText, Long orderId);
}