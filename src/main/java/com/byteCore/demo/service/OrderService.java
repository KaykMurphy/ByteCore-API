package com.byteCore.demo.service;

import com.byteCore.demo.domain.*;
import com.byteCore.demo.dto.request.OrderCreateDTO;
import com.byteCore.demo.dto.request.OrderItemRequestDTO;
import com.byteCore.demo.enums.DeliveryType;
import com.byteCore.demo.enums.OrderStatus;
import com.byteCore.demo.enums.ProductStatus;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class    OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderEntity createOrder(UserEntity user, OrderCreateDTO dto) {

        log.info("Criando pedido para usuário: {}", user.getEmail());

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .deliveryEmail(user.getEmail()) // Usa email do usuário
                .build();

        for (OrderItemRequestDTO itemDto : dto.getItems()) {

            ProductEntity product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Produto não encontrado: " + itemDto.getProductId()
                    ));

            if (!product.isActive()) {
                throw new IllegalStateException(
                        "Produto inativo: " + product.getTitle()
                );
            }

            if (product.getStatus() != ProductStatus.APPROVED) {
                throw new IllegalStateException(
                        "Produto não disponível para venda. Status: " +
                                product.getStatus()
                );
            }

            if (product.getSeller() != null && !product.getSeller()
                    .isVerifiedSeller()) {
                throw new IllegalStateException(
                        "Vendedor não está verificado"
                );
            }


            if (product.getDeliveryType() == DeliveryType.AUTOMATIC) {
                if (!product.hasStock(itemDto.getQuantity())) {
                    throw new IllegalStateException(
                            "Estoque insuficiente para: " + product.getTitle() +
                                    " (Disponível: " +
                                    product.getAvailableStock() + ")"
                    );
                }
            }

                OrderItemEntity item = OrderItemEntity.builder()
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .price(product.getPrice())
                    .delivered(false)
                    .build();

            order.addItem(item);
        }

        order.calculateTotal();

        order = orderRepository.save(order);

        log.info("Pedido criado com sucesso: ID={}, Total=R$ {}",
                order.getId(), order.getTotal());

        return order;
    }

    @Transactional(readOnly = true)
    public OrderEntity findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pedido não encontrado: " + orderId
                ));
    }

    @Transactional(readOnly = true)
    public List<OrderEntity> findByUser(UserEntity user) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public void markAsPaid(Long orderId) {
        OrderEntity order = findById(orderId);

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("Tentativa de marcar pedido {} como pago, mas status é: {}",
                    orderId, order.getStatus());
            return;
        }

        order.markAsPaid();
        orderRepository.save(order);

        log.info("Pedido {} marcado como PAGO", orderId);
    }

    @Transactional
    public void markAsDelivered(Long orderId) {
        OrderEntity order = findById(orderId);

        if (order.getStatus() != OrderStatus.PAID) {
            log.warn("Tentativa de marcar pedido {} como entregue, mas status é: {}",
                    orderId, order.getStatus());
            return;
        }

        order.markAsDelivered();
        orderRepository.save(order);

        log.info("Pedido {} marcado como ENTREGUE", orderId);
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        OrderEntity order = findById(orderId);

        if (!order.canBeCancelled()) {
            throw new IllegalStateException(
                    "Pedido não pode ser cancelado. Status: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(java.time.Instant.now());
        orderRepository.save(order);

        log.info("Pedido {} cancelado. Motivo: {}", orderId, reason);
    }
}