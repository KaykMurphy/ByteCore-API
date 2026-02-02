package com.byteCore.demo.dto.request;

import com.byteCore.demo.enums.ProductType;

public record ProductUpdateDTO (

        String title,
        String description,
        String imageUrl,
        Boolean active,
        ProductType type
)
{}


