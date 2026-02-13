package com.byteCore.demo.dto.mapper;


import com.byteCore.demo.domain.Document;
import com.byteCore.demo.dto.response.DocumentResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    // entidade > response
    DocumentResponseDTO toResponseDTO(Document entity);

    // Converte lista de entidades em lista de DTOs de resposta
    List<DocumentResponseDTO> toResponseDTOList(List<Document> entity);
}
