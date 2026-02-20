package com.byteCore.demo.dto.mapper;

import com.byteCore.demo.domain.ReviewEntity;
import com.byteCore.demo.dto.response.ReviewResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "reviewer.id", target = "reviewerId")
    @Mapping(source = "reviewer.name", target = "reviewerName")
    @Mapping(source = "reviewedUser.id", target = "reviewedUserId")
    @Mapping(source = "reviewedUser.name", target = "reviewedUserName")
    ReviewResponseDTO toResponseDTO(ReviewEntity entity);


    List<ReviewResponseDTO> toResponseDTOList(List<ReviewEntity> entities);


}
