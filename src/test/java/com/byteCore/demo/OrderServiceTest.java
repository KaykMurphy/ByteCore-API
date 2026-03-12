package com.byteCore.demo;

import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.domain.OrderItemEntity;
import com.byteCore.demo.domain.ProductEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.request.OrderCreateDTO;
import com.byteCore.demo.dto.request.OrderItemRequestDTO;
import com.byteCore.demo.enums.DeliveryType;
import com.byteCore.demo.enums.ProductStatus;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.ProductRepository;
import com.byteCore.demo.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldCreateOrderSuccessfully() {

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("email@gmail.com");

        OrderEntity order = new OrderEntity();
        order.setId(1L);

        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setStatus(ProductStatus.APPROVED);
        product.setActive(true);
        product.setDeliveryType(DeliveryType.MANUAL);
        product.setPrice(new BigDecimal("100.321321"));

        OrderItemRequestDTO orderItemRequestDTO = new OrderItemRequestDTO();
        orderItemRequestDTO.setProductId(product.getId());
        orderItemRequestDTO.setQuantity(1);

        OrderCreateDTO itemDto = new OrderCreateDTO();
        itemDto.setItems(List.of(orderItemRequestDTO));


        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));


        when(orderRepository.save(Mockito.any(OrderEntity.class)))
                .thenReturn(order);

        //act
        OrderEntity result = orderService.createOrder(userEntity, itemDto);


        //assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getId());

        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).save(Mockito.any(OrderEntity.class));

    }


    @Test
    void createOrder_shouldThrowException_whenProductIsInactive() {

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("email.gmail.com");

        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setTitle("title");
        product.setActive(false);

        OrderItemRequestDTO orderItemRequestDTO = new OrderItemRequestDTO();
        orderItemRequestDTO.setProductId(product.getId());

        OrderCreateDTO itemDto = new OrderCreateDTO();
        itemDto.setItems(List.of(orderItemRequestDTO));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        IllegalStateException exception =
                Assertions.assertThrows(IllegalStateException.class,
                () -> orderService.createOrder(userEntity, itemDto));

        Assertions.assertEquals("Produto inativo: " +
                product.getTitle(), exception.getMessage());

        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void createOrder_shouldThrowException_whenProductStatusIsNotApproved() {

        UserEntity userEntity = new  UserEntity();
        userEntity.setEmail("email.gmail.com");

        ProductEntity product = new  ProductEntity();
        product.setId(1L);
        product.setTitle("title");
        product.setActive(true);
        product.setStatus(ProductStatus.REJECTED);

        OrderItemRequestDTO orderItemRequestDTO = new OrderItemRequestDTO();
        orderItemRequestDTO.setProductId(product.getId());

        OrderCreateDTO itemDto = new OrderCreateDTO();
        itemDto.setItems(List.of(orderItemRequestDTO));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        IllegalStateException exception =
                Assertions.assertThrows(
                        IllegalStateException.class,
                        () -> orderService.createOrder(userEntity, itemDto)
                );

        Assertions.assertEquals(
                "Produto não disponível para venda. Status: " +
                        product.getStatus(),  exception.getMessage()
                );

        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

}


