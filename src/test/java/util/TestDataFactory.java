package util;

import com.byteCore.demo.domain.*;
import com.byteCore.demo.dto.request.ProductCreateDTO;
import com.byteCore.demo.dto.request.ProductUpdateDTO;
import com.byteCore.demo.dto.request.ReviewRequestDTO;
import com.byteCore.demo.dto.response.ReviewResponseDTO;
import com.byteCore.demo.enums.ProductType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TestDataFactory {

    public static ProductCreateDTO validProductCreateDTO() {
        return new ProductCreateDTO(
                "Title",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }


    public static ProductUpdateDTO  validProductUpdateDTO() {
        return new ProductUpdateDTO(
                "Novo título",
                "Nova descrição",
                "img.png",
                true,
                ProductType.EDUCATION,
                new BigDecimal("100.00"),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static UserEntity validUser() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setName("Usuário Genérico");
        user.setEmail("usuario@teste.com");
        return user;
    }

    public static ReviewRequestDTO validReviewRequestDTO() {
        return new ReviewRequestDTO(1L, 5, "Bom - Teste");
    }

    public static ReviewResponseDTO validReviewResponseDTO(Long orderId, UserEntity reviewer, UserEntity seller) {
        return new ReviewResponseDTO(
                UUID.randomUUID(),
                orderId,
                reviewer.getId(),
                reviewer.getName(),
                seller.getId(),
                seller.getName(),
                5,
                "Bom - Teste",
                true,
                java.time.Instant.now(),
                java.time.Instant.now(),
                0
        );
    }

    public static OrderEntity validDeliveredOrder(UserEntity buyer, UserEntity seller, Long orderId) {
        ProductEntity product = new ProductEntity();
        product.setSeller(seller);

        OrderItemEntity item = new OrderItemEntity();
        item.setProduct(product);

        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setUser(buyer);
        order.setItems(List.of(item));
        order.setDeliveredAt(Instant.now());

        return order;
    }

    public static ReviewEntity validReviewEntity(UUID reviewId, UserEntity reviewer, UserEntity seller) {
        ReviewEntity review = new ReviewEntity();
        review.setId(reviewId);
        review.setReviewer(reviewer);
        review.setReviewedUser(seller);
        review.setRating(4);
        review.setComment("comment antigo");

        return review;
    }
}
