package com.byteCore.demo.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponseDTO(

        UUID id,
        Long orderId,
        UUID reviewerId,
        String reviewerName,
        UUID reviewedUserId,
        String reviewedUserName,
        Integer rating,
        String comment,
        Boolean verifiedPurchase,
        Instant createdAt,
        Instant updatedAt,
        Integer editCount

) {
}
