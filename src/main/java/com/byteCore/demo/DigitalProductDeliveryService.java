package com.byteCore.demo;

import com.byteCore.demo.domain.*;
import com.byteCore.demo.enums.DeliveryStatus;
import com.byteCore.demo.enums.DeliveryType;
import com.byteCore.demo.repository.DeliveryLogRepository;
import com.byteCore.demo.repository.OrderRepository;
import com.byteCore.demo.repository.ProductRepository;
import com.byteCore.demo.repository.ProductStockRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalProductDeliveryService {

    private final ProductStockRepository productStockRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final OrderService orderService;
    private final DeliveryLogRepository  deliveryLogRepository;

    @Transactional
    public void deliverOrder(OrderEntity order) {

        log.info("Iniciando entrega automática do pedido #{}", order.getId());

        try {
            List<String> deliveredProducts = new ArrayList<>();

            for (OrderItemEntity item : order.getItems()) {
                ProductEntity product = item.getProduct();

                if (product.getDeliveryType() == DeliveryType.AUTOMATIC) {
                    String content = deliverAutomatically(item);
                    deliveredProducts.add(formatProductForEmail(item, content));
                } else {
                    log.info("Produto {} requer entrega manual", product.getTitle());
                    notifyAdminForManualDelivery(item);
                }
            }

            if (!deliveredProducts.isEmpty()) {
                emailService.sendDigitalProductsEmail(
                        order.getDeliveryEmail(),
                        order.getId(),
                        deliveredProducts
                );

                orderService.markAsDelivered(order.getId());

                DeliveryLogEntity successLog = DeliveryLogEntity.builder()
                        .order(order)
                        .status(DeliveryStatus.SUCCESS)
                        .build();
                deliveryLogRepository.save(successLog);

                log.info("Pedido #{} entregue com sucesso", order.getId());
            }

        } catch (OptimisticLockException e) {

            DeliveryLogEntity deliveryLog = DeliveryLogEntity.builder()
                    .order(order)
                    .status(DeliveryStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .nextRetryAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                    .build();

            deliveryLogRepository.save(deliveryLog);

            log.error("Concorrência detectada ao entregar pedido #{}", order.getId());
            throw new IllegalStateException(
                    "Produto esgotou durante a compra. Por favor, tente novamente."
            );
        } catch (Exception e) {

            DeliveryLogEntity failedLog = DeliveryLogEntity.builder()
                    .order(order)
                    .status(DeliveryStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .nextRetryAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                    .build();

            deliveryLogRepository.save(failedLog);

            log.error("Erro ao entregar pedido #{}: {}", order.getId(), e.getMessage(), e);
            notifyAdminAboutDeliveryError(order, e.getMessage());
        }
    }

    private String deliverAutomatically(OrderItemEntity item) {

        ProductEntity product = item.getProduct();
        int quantity = item.getQuantity();

        log.info("Entregando {} unidade(s) de {}", quantity, product.getTitle());

        List<ProductStockEntity> stockItems = productStockRepository
                .findAvailableByProductId(
                        product.getId(),
                        PageRequest.of(0, quantity)
                );

        if (stockItems.size() < quantity) {
            throw new IllegalStateException(
                    "Estoque insuficiente para " + product.getTitle() +
                            ". Necessário: " + quantity + ", Disponível: " + stockItems.size()
            );
        }

        StringBuilder content = new StringBuilder();

        for (int i = 0; i < quantity; i++) {
            ProductStockEntity stockItem = stockItems.get(i);

            stockItem.markAsSold(item);
            productStockRepository.save(stockItem);

            if (i > 0) content.append("\n");
            content.append(stockItem.getContent());
        }

        product.decrementStock(quantity);
        product.incrementSales(quantity);
        productRepository.save(product);

        item.markAsDelivered(content.toString());

        log.info("Item entregue: {} ({}x)", product.getTitle(), quantity);

        return content.toString();
    }

    private String formatProductForEmail(OrderItemEntity item, String content) {
        ProductEntity product = item.getProduct();

        StringBuilder formatted = new StringBuilder();
        formatted.append("").append(product.getTitle()).append("\n");
        formatted.append("Quantidade: ").append(item.getQuantity()).append("\n");
        formatted.append("Plataforma: ")
                .append(product.getPlatform() != null ? product.getPlatform() : "N/A")
                .append("\n\n");

        formatted.append("SEU(S) PRODUTO(S):\n");
        formatted.append(content).append("\n");

        if (product.getActivationInstructions() != null) {
            formatted.append("\nINSTRUÇÕES:\n");
            formatted.append(product.getActivationInstructions()).append("\n");
        }

        formatted.append("\n").append("─".repeat(50)).append("\n\n");

        return formatted.toString();
    }

    private void notifyAdminForManualDelivery(OrderItemEntity item) {
        log.info(
                "Notificando admin sobre entrega manual: Pedido #{}, Produto: {}",
                item.getOrder().getId(),
                item.getProduct().getTitle()
        );
    }

    private void notifyAdminAboutDeliveryError(OrderEntity order, String errorMessage) {
        log.error("ERRO NA ENTREGA - Pedido #{}: {}", order.getId(), errorMessage);
    }
}
