package com.byteCore.demo.dto.mapper;

import com.byteCore.demo.domain.Product;
import com.byteCore.demo.dto.request.ProductCreateDTO;
import com.byteCore.demo.dto.response.ProductResponseDTO;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponseDTO toDto(Product product);

    Product toEntity(ProductCreateDTO dto);
}
