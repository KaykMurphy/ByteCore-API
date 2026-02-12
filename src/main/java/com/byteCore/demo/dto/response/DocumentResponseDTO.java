package com.byteCore.demo.dto.response;

import com.byteCore.demo.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record DocumentResponseDTO(

        Long id,
        DocumentType documentType,
        String filename,
        Long fileSizeBytes,


        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant uploadedAt,

        Boolean verified // Admin validou esse documento?
) {
}
