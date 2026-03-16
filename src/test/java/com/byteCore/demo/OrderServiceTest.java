package com.byteCore.demo;

import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.domain.OrderItemEntity;
import com.byteCore.demo.domain.ProductEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.dto.request.OrderCreateDTO;
import com.byteCore.demo.dto.request.OrderItemRequestDTO;
import com.byteCore.demo.enums.DeliveryType;
import com.byteCore.demo.enums.OrderStatus;
import com.byteCore.demo.enums.ProductStatus;
import com.byteCore.demo.enums.Role;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.ProductRepository;
import com.byteCore.demo.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    void createOrder_shouldThrowException_whenProductNotFound() {

        UserEntity buyer = new UserEntity();
        buyer.setEmail("comprador@gmail.com");

        OrderItemRequestDTO orderItemRequestDTO = new OrderItemRequestDTO();
        orderItemRequestDTO.setProductId(1L);

        OrderCreateDTO itemDto = new OrderCreateDTO();
        itemDto.setItems(List.of(orderItemRequestDTO));

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception =
                Assertions.assertThrows(
                        EntityNotFoundException.class,
                        () -> orderService.createOrder(buyer, itemDto)
                );

        Assertions.assertEquals(
                "Produto não encontrado: 1",
                exception.getMessage()
        );

        verify(orderRepository, never()).save(any(OrderEntity.class));
    }


    @Test
    void createOrder_shouldCreateOrderSuccessfully() {

        UserEntity buyer = new UserEntity();
        buyer.setEmail("comprador@gmail.com");

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
        OrderEntity result = orderService.createOrder(buyer, itemDto);

        //assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1L, result.getId());

        verify(productRepository, times(1)).findById(product.getId());
        verify(orderRepository, times(1)).save(Mockito.any(OrderEntity.class));
    }


    @Test
    void createOrder_shouldThrowException_whenProductIsInactive() {

        UserEntity buyer = new UserEntity();
        buyer.setEmail("comprador@gmail.com");

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
                        () -> orderService.createOrder(buyer, itemDto));

        Assertions.assertEquals("Produto inativo: " +
                product.getTitle(), exception.getMessage());

        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void createOrder_shouldThrowException_whenProductStatusIsNotApproved() {

        UserEntity buyer = new UserEntity();
        buyer.setEmail("comprador@gmail.com");

        ProductEntity product = new ProductEntity();
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
                        () -> orderService.createOrder(buyer, itemDto)
                );

        Assertions.assertEquals(
                "Produto não disponível para venda. Status: " +
                        product.getStatus(), exception.getMessage()
        );

        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void createOrder_shouldThrowException_whenSellerIsNotVerified() {

        UserEntity buyer = new UserEntity();
        buyer.setEmail("comprador@gmail.com");

        UserEntity seller = new UserEntity();
        seller.setEmail("vendedor@gmail.com");

        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setTitle("title");
        product.setActive(true);
        product.setStatus(ProductStatus.APPROVED);
        product.setSeller(seller);

        OrderItemRequestDTO orderItemRequestDTO = new OrderItemRequestDTO();
        orderItemRequestDTO.setProductId(product.getId());

        OrderCreateDTO itemDto = new OrderCreateDTO();
        itemDto.setItems(List.of(orderItemRequestDTO));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        IllegalStateException exception =
                Assertions.assertThrows(
                        IllegalStateException.class,
                        () -> orderService.createOrder(buyer, itemDto)
                );

        Assertions.assertEquals(
                "Vendedor não está verificado",
                exception.getMessage()
        );

        verify(orderRepository, never()).save(any(OrderEntity.class));
    }


    @Test
    void createOrder_shouldThrowException_whenInsufficientStock() {

        UserEntity buyer = new UserEntity();
        buyer.setEmail("comprador@gmail.com");

        UserEntity seller = new UserEntity();
        seller.setEmail("vendedor@gmail.com");
        seller.setRole(Role.VERIFIED_SELLER);

        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setTitle("title");
        product.setActive(true);
        product.setStatus(ProductStatus.APPROVED);
        product.setDeliveryType(DeliveryType.AUTOMATIC);
        product.setSeller(seller);
        product.setAvailableStock(5L);

        OrderItemRequestDTO itemDto = new OrderItemRequestDTO();
        itemDto.setProductId(product.getId());
        itemDto.setQuantity(10);

        OrderCreateDTO order = new OrderCreateDTO();
        order.setItems(List.of(itemDto));

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        IllegalStateException exception =
                Assertions.assertThrows(
                        IllegalStateException.class,
                        () -> orderService.createOrder(buyer, order)
                );

        Assertions.assertEquals(
                "Estoque insuficiente para: " + product.getTitle()
                        + " (Disponível: " + product.getAvailableStock() + ")",
                exception.getMessage()
        );

        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void findById_shouldFindByIdSuccessfully() {
        OrderEntity order = new OrderEntity();
        order.setId(1L);

        when(orderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        OrderEntity result = orderService.findById(order.getId());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(order.getId(), result.getId());

        verify(orderRepository, times(1)).findById(order.getId());
    }

    @Test
    void findById_shouldThrowException_whenOrderNotFound() {

        Long idBusca = 1L;

        when(orderRepository.findById(idBusca))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception =
                Assertions.assertThrows(
                        EntityNotFoundException.class,
                        () -> orderService.findById(idBusca)
                );

        Assertions.assertEquals(
                "Pedido não encontrado: " + idBusca,
                exception.getMessage()
        );

        verify(orderRepository, times(1)).findById(idBusca);
    }

    @Test
    void findByUser_shouldFindByUserSuccessfully() {

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());

        OrderEntity order = new OrderEntity();
        order.setId(1L);

        List<OrderEntity> list = new ArrayList<>();
        list.add(order);

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(list);

        List<OrderEntity> result = orderService.findByUser(user);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(order.getId(), result.get(0).getId());

        verify(orderRepository, times(1))
                .findByUserIdOrderByCreatedAtDesc(user.getId());

    }

    @Test
    void markAsDelivered_shouldMarkAsDeliveredSuccessfully() {


        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        orderService.markAsDelivered(order.getId());

        Assertions.assertEquals(OrderStatus.DELIVERED, order.getStatus());

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void markAsDelivered_shouldThrowException_whenOrderNotFound() {

        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setStatus(OrderStatus.CANCELLED);


        when(orderRepository.findById(order.getId()))
                .thenReturn(Optional.of(order));

        orderService.markAsDelivered(order.getId());

        Assertions.assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(orderRepository, never()).save(any(OrderEntity.class));
    }





}