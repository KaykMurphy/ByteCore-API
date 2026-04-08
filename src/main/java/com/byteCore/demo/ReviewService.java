package com.byteCore.demo;

import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.domain.ProductEntity;
import com.byteCore.demo.domain.ReviewEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.mapper.ReviewMapper;
import com.byteCore.demo.dto.request.ReviewRequestDTO;
import com.byteCore.demo.dto.response.ReviewResponseDTO;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.ReviewRepository;
import com.byteCore.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReviewResponseDTO createReview(UserEntity reviewer, ReviewRequestDTO dto) {

        OrderEntity order = orderRepository.findById(dto.orderId())
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + dto.orderId()));

        if (!order.getUser().getId().equals(reviewer.getId())) {
            throw new IllegalStateException("Não é seu pedido");
        }

        if (!order.isDelivered()) {
            throw new IllegalStateException("Pedido ainda não entregue");
        }

        if (reviewRepository.existsByOrderIdAndReviewerId(order.getId(), reviewer.getId())) {
            throw new IllegalStateException("Já avaliou este pedido");
        }

        // identifica produto
        ProductEntity product = order.getItems().get(0).getProduct();

        //identifica o vendedor
        UserEntity seller = product.getSeller();

        ReviewEntity reviewEntity = ReviewEntity.builder()
                .order(order)
                .reviewer(reviewer)
                .reviewedUser(seller)
                .product(product)
                .rating(dto.rating())
                .comment(dto.comment())
                .verifiedPurchase(true)
                .editCount(0)
                .build();

        reviewRepository.save(reviewEntity);

        recalculateSellerRating(seller.getId());

        return reviewMapper.toResponseDTO(reviewEntity);
    }

    public ReviewResponseDTO updateReview(
            UUID reviewId, UserEntity reviewer, ReviewRequestDTO dto) {

        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review não encontrado"));

        if (!review.getReviewer().getId().equals(reviewer.getId())) {
            throw new IllegalStateException("Não é sua review");
        }

        review.updateReview(dto.rating(), dto.comment());

        recalculateSellerRating(review.getReviewedUser().getId());

        return  reviewMapper.toResponseDTO(reviewRepository.save(review));
    }

    public List<ReviewResponseDTO> getReviewsByUser(UUID userId){

        List<ReviewEntity> reviews = reviewRepository.findByReviewedUserId(userId);

        return reviewMapper.toResponseDTOList(reviews);
    }

    private void recalculateSellerRating(UUID sellerId) {

        Double newAverage = reviewRepository.getAverageRatingByUserId(sellerId);

        UserEntity seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Vendedor não encontrado: " + sellerId));

        if (newAverage != null) {
            seller.setAverageRating(BigDecimal.valueOf(newAverage));

            userRepository.save(seller);
        }
    }
}





