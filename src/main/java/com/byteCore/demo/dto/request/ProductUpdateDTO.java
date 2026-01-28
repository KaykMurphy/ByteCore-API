package com.byteCore.demo.dto.request;

public record ProductUpdateDTO (

        String title,
        String description,
        String imageUrl,
        Boolean active
)
{}


