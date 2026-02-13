package com.byteCore.demo.dto.mapper;

import com.byteCore.demo.domain.SellerVerification;
import com.byteCore.demo.dto.request.SellerVerificationRequestDTO;
import com.byteCore.demo.dto.response.SellerVerificationResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SellerVerificationMapper {

    @Mapping(target = "cpf", source = "cpf")
    @Mapping(target = "documentCount", source = "documentCount")
    SellerVerificationResponseDTO toResponseDTO(SellerVerification entity); // entidade > response

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    SellerVerification toEntity(SellerVerificationRequestDTO dto); // request > entidade

    default String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }
}


