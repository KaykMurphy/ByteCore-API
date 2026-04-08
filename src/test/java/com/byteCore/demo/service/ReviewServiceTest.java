package com.byteCore.demo.service;

import com.byteCore.demo.domain.*;
import com.byteCore.demo.dto.mapper.ReviewMapper;
import com.byteCore.demo.dto.request.ReviewRequestDTO;
import com.byteCore.demo.dto.response.ReviewResponseDTO;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.ReviewRepository;
import com.byteCore.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.TestDataFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReview_shouldCreateReviewSuccessfully_whenDataIsValid() {

        UserEntity reviewer = TestDataFactory.validUser();
        reviewer.setName("Comprador Teste");

        UserEntity seller = TestDataFactory.validUser();
        seller.setName("Vendedor Teste");

        ReviewRequestDTO dto = TestDataFactory.validReviewRequestDTO();

        OrderEntity order = TestDataFactory.validDeliveredOrder(reviewer, seller, dto.orderId());

        when(orderRepository.findById(dto.orderId()))
                .thenReturn(Optional.of(order));

        when(reviewRepository.existsByOrderIdAndReviewerId(order.getId(), reviewer.getId()))
                .thenReturn(false);

        when(reviewRepository.getAverageRatingByUserId(seller.getId()))
                .thenReturn(4.8);

        when(userRepository.findById(seller.getId()))
                .thenReturn(Optional.of(seller));

        ReviewResponseDTO expectedResponse =
                TestDataFactory.validReviewResponseDTO(dto.orderId(), reviewer, seller);

        when(reviewMapper.toResponseDTO(any(ReviewEntity.class)))
                .thenReturn(expectedResponse);

        ReviewResponseDTO result = reviewService.createReview(reviewer, dto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedResponse, result);

        verify(reviewRepository, times(1))
                .save(any(ReviewEntity.class));

        verify(reviewMapper, times(1))
                .toResponseDTO(any(ReviewEntity.class));

        verify(userRepository, times(1))
                .save(seller);
    }

    @Test
    void createReview_shouldThrowException_whenOrderDoesNotExist() {

        UserEntity reviewer = TestDataFactory.validUser();
        reviewer.setId(UUID.randomUUID());

        ReviewRequestDTO dto = TestDataFactory.validReviewRequestDTO();
        
        when(orderRepository.findById(dto.orderId()))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.createReview(reviewer, dto)
        ); 
        
        Assertions.assertEquals(
                "Pedido não encontrado: " +
                 dto.orderId(), exception.getMessage()
        );
        
        Assertions.assertNotNull(exception);
        
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_shouldThrowException_whenReviewerIsNotOrderOwner(){

        UserEntity impostor = TestDataFactory.validUser();
        impostor.setId(UUID.randomUUID());

        UserEntity trueOwner = TestDataFactory.validUser();
        trueOwner.setId(UUID.randomUUID());

        ReviewRequestDTO dto = TestDataFactory.validReviewRequestDTO();

        OrderEntity order = TestDataFactory.validDeliveredOrder(
                trueOwner,
                TestDataFactory.validUser(),
                dto.orderId()
        );

        when(orderRepository.findById(dto.orderId()))
                .thenReturn(Optional.of(order));


        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(impostor, dto)
        );

        Assertions.assertEquals(
                "Não é seu pedido",
                exception.getMessage()
        );


        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_shouldThrowException_whenOrderIsNotDelivered() {

        UserEntity reviewer = TestDataFactory.validUser();
        reviewer.setId(UUID.randomUUID());

        ReviewRequestDTO dto = TestDataFactory.validReviewRequestDTO();

        OrderEntity order =  TestDataFactory.validDeliveredOrder(
                reviewer,
                TestDataFactory.validUser(),
                dto.orderId()
        );

        order.setDeliveredAt(null);

        when(orderRepository.findById(dto.orderId()))
                .thenReturn(Optional.of(order));


        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(reviewer, dto)
        );


        Assertions.assertEquals(
                "Pedido ainda não entregue",
                exception.getMessage()
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_shouldThrowException_whenUserAlreadyReviewedOrder() {

        UserEntity reviewer = TestDataFactory.validUser();
        reviewer.setAverageRating(new BigDecimal(5.0));


        ReviewRequestDTO dto = TestDataFactory.validReviewRequestDTO();

        OrderEntity order = TestDataFactory.validDeliveredOrder(
                reviewer,
                TestDataFactory.validUser(),
                dto.orderId()
        );

        when(reviewRepository.existsByOrderIdAndReviewerId(order.getId(), reviewer.getId()))
                .thenReturn(true);

        when(orderRepository.findById(dto.orderId()))
                .thenReturn(Optional.of(order));

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> reviewService.createReview(reviewer, dto)
        );

        Assertions.assertEquals(
                "Já avaliou este pedido", exception.getMessage()
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReview_shouldUpdateSuccessfully_whenDataIsValid() {

        UserEntity reviewer = TestDataFactory.validUser();
        reviewer.setId(UUID.randomUUID());

        UserEntity seller =  TestDataFactory.validUser();
        seller.setId(UUID.randomUUID());

        ReviewRequestDTO dto = TestDataFactory.validReviewRequestDTO();
        UUID reviewId = UUID.randomUUID();

        ReviewEntity review = TestDataFactory.validReviewEntity(
                reviewId,
                reviewer,
                seller
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        when(reviewRepository.getAverageRatingByUserId(seller.getId()))
                .thenReturn(4.9);

        when(userRepository.findById(seller.getId()))
                .thenReturn(Optional.of(seller));

        when(reviewRepository.save(any(ReviewEntity.class)))
                .thenReturn(review);

        ReviewResponseDTO expectedResponse = TestDataFactory.validReviewResponseDTO(
                1L, reviewer, seller
        );

        when(reviewMapper.toResponseDTO(any(ReviewEntity.class)))
                .thenReturn(expectedResponse);

        ReviewResponseDTO result = reviewService.updateReview(reviewId, reviewer, dto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expectedResponse, result);

        ArgumentCaptor<ReviewEntity> captor = ArgumentCaptor.forClass(ReviewEntity.class);

        verify(reviewRepository, times(1)).save(captor.capture());

        ReviewEntity capturedReview = captor.getValue();

        Assertions.assertEquals(reviewId, capturedReview.getId());
        Assertions.assertEquals(reviewer, capturedReview.getReviewer());
        Assertions.assertEquals(dto.rating(), capturedReview.getRating());
        Assertions.assertEquals(dto.comment(), capturedReview.getComment());

        verify(userRepository, times(1)).save(seller);
        verify(reviewMapper, times(1)).toResponseDTO(review);
    }


    @Test
    void updateReview_shouldThrowException_whenReviewDoesNotExist() {

        UserEntity reviewer = TestDataFactory.validUser();

        ReviewRequestDTO dto = TestDataFactory.validReviewRequestDTO();

        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());


        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> reviewService.updateReview(reviewId, reviewer, dto)
        );

        Assertions.assertEquals(
                "Review não encontrado",
                exception.getMessage()
        );


        verify(reviewRepository, never()).save(any());
    }


    @Test
    void updateReview_shouldThrowException_whenUserIsNotOwner() {


        UserEntity seller = TestDataFactory.validUser();
        seller.setId(UUID.randomUUID());

        UserEntity impostor =  TestDataFactory.validUser();
        impostor.setId(UUID.randomUUID());

        UserEntity trueOwner = TestDataFactory.validUser();
        trueOwner.setId(UUID.randomUUID());

        UUID reviewId = UUID.randomUUID();

        ReviewRequestDTO dto = TestDataFactory.validReviewRequestDTO();

        ReviewEntity review = TestDataFactory.validReviewEntity(
                reviewId,
                trueOwner,
                seller
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> reviewService.updateReview(reviewId, impostor, dto)
        );

        Assertions.assertEquals(
                "Não é sua review",
                exception.getMessage()
        );

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getReviewsByUser_shouldReturnReviewList_whenUserHasReviews() {

        UUID userId = UUID.randomUUID();

        UserEntity reviewer = TestDataFactory.validUser();
        reviewer.setId(UUID.randomUUID());

        UserEntity seller = TestDataFactory.validUser();
        seller.setId(userId);

        ReviewEntity review = TestDataFactory.validReviewEntity(
                UUID.randomUUID(), reviewer, seller);

        List<ReviewEntity> entityList = List.of(review);

        when(reviewRepository.findByReviewedUserId(userId))
                .thenReturn(entityList);

        ReviewResponseDTO responseDTO = TestDataFactory.validReviewResponseDTO(
                1L, reviewer, seller);

        List<ReviewResponseDTO> expectedResponseList = List.of(responseDTO);

        when(reviewMapper.toResponseDTOList(entityList))
                .thenReturn(expectedResponseList);

        List<ReviewResponseDTO> result = reviewService.getReviewsByUser(userId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(expectedResponseList, result);

        verify(reviewRepository, times(1))
                .findByReviewedUserId(userId);

        verify(reviewMapper, times(1))
                .toResponseDTOList(entityList);
    }


}