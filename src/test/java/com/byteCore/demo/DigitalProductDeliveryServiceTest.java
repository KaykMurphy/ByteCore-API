package com.byteCore.demo;

import com.byteCore.demo.domain.*; // Importei com * pra ficar mais limpo
import com.byteCore.demo.enums.DeliveryStatus;
import com.byteCore.demo.enums.DeliveryType;
import com.byteCore.demo.enums.ProductStatus;
import com.byteCore.demo.repository.DeliveryLogRepository;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.ProductRepository;
import com.byteCore.demo.repository.ProductStockRepository;
import com.byteCore.demo.service.DigitalProductDeliveryService;
import com.byteCore.demo.service.EmailService;
import com.byteCore.demo.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import util.TestDataFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DigitalProductDeliveryServiceTest {

    @Mock
    private ProductStockRepository productStockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private OrderService orderService;

    @Mock
    private DeliveryLogRepository deliveryLogRepository;

    @InjectMocks
    private DigitalProductDeliveryService digitalProductDeliveryService;


    @Test
    void deliverOrder_shouldDeliverSuccessfully_whenStockIsAvailable() {

        OrderEntity order = TestDataFactory.validOrderEntity();

        ProductEntity autoProduct = TestDataFactory.validProductEntity();
        autoProduct.setId(10L);
        autoProduct.setDeliveryType(DeliveryType.AUTOMATIC);
        autoProduct.setAvailableStock(10L);

        OrderItemEntity autoItem = new OrderItemEntity();
        autoItem.setId(100L);
        autoItem.setProduct(autoProduct);
        autoItem.setQuantity(1);
        autoItem.setOrder(order);

        order.getItems().add(autoItem);

        ProductStockEntity productStock = new ProductStockEntity();
        productStock.setProduct(autoProduct);
        productStock.setContent("CHAVE-DE-ATIVACAO-123");

        ProductEntity manualProduct = TestDataFactory.validProductEntity();
        manualProduct.setId(20L);
        manualProduct.setTitle("Mentoria VIP");
        manualProduct.setDeliveryType(DeliveryType.MANUAL);

        OrderItemEntity manualItem = new OrderItemEntity();
        manualItem.setId(200L);
        manualItem.setProduct(manualProduct);
        manualItem.setQuantity(1);
        manualItem.setOrder(order);

        order.getItems().add(manualItem);

        when(productStockRepository.findAvailableByProductId(
                eq(10L),
                any(PageRequest.class)
        )).thenReturn(List.of(productStock));

        Assertions.assertDoesNotThrow(() ->
                digitalProductDeliveryService.deliverOrder(order)
        );

        verify(orderService, times(1)).markAsDelivered(order.getId());

        verify(emailService, times(1)).sendDigitalProductsEmail(
                eq(order.getDeliveryEmail()),
                eq(order.getId()),
                anyList()
        );

        ArgumentCaptor<DeliveryLogEntity> logCaptor = ArgumentCaptor.forClass(DeliveryLogEntity.class);
        verify(deliveryLogRepository, times(1)).save(logCaptor.capture());

        Assertions.assertEquals(DeliveryStatus.SUCCESS, logCaptor.getValue().getStatus());
    }



    @Test
    void deliverOrder_shouldLogFailure_whenStockIsInsufficient() {

        OrderEntity order = TestDataFactory.validOrderEntity();

        ProductEntity product = TestDataFactory.validProductEntity();
        product.setDeliveryType(DeliveryType.AUTOMATIC);

        OrderItemEntity item = new OrderItemEntity();
        item.setId(100L);
        item.setProduct(product);
        item.setQuantity(2);
        item.setOrder(order);

        order.getItems().add(item);

        ProductStockEntity productStock = new ProductStockEntity();
        productStock.setId(30L);
        productStock.setContent("content ativação");

        when(productStockRepository.findAvailableByProductId(
                eq(10L),
                any(PageRequest.class)
        )).thenReturn(List.of(productStock));


        Assertions.assertDoesNotThrow(() ->
                digitalProductDeliveryService.deliverOrder(order)
        );


        ArgumentCaptor<DeliveryLogEntity> logCaptor = ArgumentCaptor.forClass(DeliveryLogEntity.class);
        verify(deliveryLogRepository, times(1)).save(logCaptor.capture());

        DeliveryLogEntity capturedLog = logCaptor.getValue();

        Assertions.assertEquals(DeliveryStatus.FAILED, capturedLog.getStatus());

        Assertions.assertTrue(capturedLog.getErrorMessage().contains("Estoque insuficiente"));

        verify(emailService, never()).sendDigitalProductsEmail(any(), any(), any());

        verify(orderService, never()).markAsDelivered(any());
    }






}