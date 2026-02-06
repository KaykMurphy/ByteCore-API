package com.byteCore.demo.service;

import com.byteCore.demo.domain.*;
import com.byteCore.demo.dto.request.OrderCreateDTO;
import com.byteCore.demo.enums.OrderStatus;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order createOrder(User user, OrderCreateDTO dto) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        for (var itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDto.getProductId()));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setPrice(product.getPrice());

            item.calculateSubtotal();

            order.addItem(item);
        }

        order.calculateTotal();
        return orderRepository.save(order);
    }
}