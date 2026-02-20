package com.byteCore.demo.dto.mapper;


import com.byteCore.demo.domain.DocumentEntity;
import com.byteCore.demo.dto.response.DocumentResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    // entidade > response
    DocumentResponseDTO toResponseDTO(DocumentEntity entity);

    // Converte lista de entidades em lista de DTOs de resposta
    List<DocumentResponseDTO> toResponseDTOList(List<DocumentEntity> entity);
}
