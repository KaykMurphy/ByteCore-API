package com.byteCore.demo.dto.request;

/*
 * DTO para consulta de dados
 */

public record PaymentStatusDTO (
        Long paymentId,
        String status,
        boolean isPaid
) {



}
