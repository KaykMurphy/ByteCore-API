package com.byteCore.demo.dto.mapper;

import com.byteCore.demo.domain.WithdrawalRequestEntity;
import com.byteCore.demo.dto.response.WithdrawalResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WithdrawalMapper {


    WithdrawalResponseDTO toResponseDTO(WithdrawalRequestEntity entity);

    List<WithdrawalResponseDTO> toResponseDTOList(List<WithdrawalRequestEntity> entities);

}

