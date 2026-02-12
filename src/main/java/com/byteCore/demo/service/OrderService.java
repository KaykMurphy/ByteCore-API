package com.byteCore.demo.service;

import com.byteCore.demo.domain.*;
import com.byteCore.demo.dto.request.OrderCreateDTO;
import com.byteCore.demo.enums.OrderStatus;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order createOrder(User user, OrderCreateDTO dto) {

        log.info("Criando pedido para usuário: {}", user.getEmail());

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .deliveryEmail(user.getEmail()) // Usa email do usuário
                .build();

        for (var itemDto : dto.getItems()) {

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Produto não encontrado: " + itemDto.getProductId()
                    ));

            if (!product.isActive()) {
                throw new IllegalStateException(
                        "Produto inativo: " + product.getTitle()
                );
            }

            if (product.getDeliveryType() == com.byteCore.demo.enums.DeliveryType.AUTOMATIC) {
                if (!product.hasStock(itemDto.getQuantity())) {
                    throw new IllegalStateException(
                            "Estoque insuficiente para: " + product.getTitle() +
                                    " (Disponível: " + product.getAvailableStock() + ")"
                    );
                }
            }

            OrderItem item = OrderItem.builder()
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
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pedido não encontrado: " + orderId
                ));
    }

    @Transactional(readOnly = true)
    public List<Order> findByUser(User user) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public void markAsPaid(Long orderId) {
        Order order = findById(orderId);

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
        Order order = findById(orderId);

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
        Order order = findById(orderId);

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