package util;

import com.byteCore.demo.SellerVerificationServiceTest;
import com.byteCore.demo.domain.*;
import com.byteCore.demo.dto.request.ProductCreateDTO;
import com.byteCore.demo.dto.request.ProductUpdateDTO;
import com.byteCore.demo.dto.request.ReviewRequestDTO;
import com.byteCore.demo.dto.request.SellerVerificationRequestDTO;
import com.byteCore.demo.dto.response.ReviewResponseDTO;
import com.byteCore.demo.enums.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    public static UserEntity validAdmin() {
        UserEntity admin = new UserEntity();
        admin.setId(UUID.randomUUID());
        admin.setName("Admin Genérico");
        admin.setEmail("admin@teste.com");
        return admin;
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

    public static SellerVerificationRequestDTO validSellerVerificationRequestDTO() {

        return new SellerVerificationRequestDTO(
                "Gutenberg Full",
                "111.111.111-11",
                "(99) 99999999",
                "444-444",
                "Endereço",
                "Perto de casa"
        );
    }

    public static SellerVerificationEntity validVerificationEntity(UserEntity user) {
        SellerVerificationEntity entity = new SellerVerificationEntity();
        entity.setId(UUID.randomUUID());
        entity.setUser(user);
        entity.setFullName("Gutenberg Full");
        entity.setCpf("111.111.111-11");
        entity.setPhoneNumber("(99) 99999999");
        entity.setCep("444-444");
        entity.setAddress("Endereço");
        entity.setAdditionalInfo("Perto de casa");

        entity.setStatus(VerificationStatus.PENDING);
        entity.setVersion(1);
        entity.setSubmittedAt(Instant.now());

        return entity;
    }

    public static DocumentEntity validDocumentEntity() {

        DocumentEntity document = new DocumentEntity();
        document.setDocumentType(DocumentType.RG);
        document.setStoredPath("caminho/do/documento.pdf");
        document.setAdminNotes("motivo ADMIN");
        document.setId(UUID.randomUUID());


        return document;
    }

    public static OrderEntity validOrderEntity() {

        OrderEntity order = new OrderEntity();
        order.setStatus(OrderStatus.PAID);
        order.setId(1L);
        order.setNotes("motivo NOTES");

        order.setItems(new ArrayList<>());

        return order;

    }

    public static ProductEntity validProductEntity() {
        ProductEntity product = new ProductEntity();
        product.setId(10L);
        product.setTitle("title");
        product.setDescription("description");
        product.setActive(true);
        product.setStatus(ProductStatus.APPROVED);

        return product;
    }

    public static PaymentEntity validPaymentEntity() {
        PaymentEntity payment = new PaymentEntity();
        payment.setId(1L);
        payment.setExternalId("ext_123456789");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setSellerAmount(new BigDecimal("90.00"));
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setMethod(PaymentMethod.PIX);
        payment.setQrCode("00020126580014br.gov.bcb.pix...");
        payment.setCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS));
        payment.setPaidAt(Instant.now().minus(2, ChronoUnit.DAYS));

        payment.setMoneyReleased(false);
        payment.setMoneyReleaseDate(Instant.now().minus(1, ChronoUnit.HOURS));

        return payment;
    }
}
