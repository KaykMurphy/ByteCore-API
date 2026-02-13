package com.byteCore.demo.dto.mapper;

import com.byteCore.demo.domain.Product;
import com.byteCore.demo.dto.request.ProductCreateDTO;
import com.byteCore.demo.dto.request.ProductUpdateDTO;
import com.byteCore.demo.dto.response.ProductResponseDTO;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "sellerName", source = "seller.name")
    ProductResponseDTO toDto(Product product);

    @Mapping(target = "seller",  ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "rejectionReason",  ignore = true)
    @Mapping(target = "submittedForReviewAt",  ignore = true)
    @Mapping(target = "approvedAt",   ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    Product toEntity(ProductCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ProductUpdateDTO dto, @MappingTarget Product entity);
}

