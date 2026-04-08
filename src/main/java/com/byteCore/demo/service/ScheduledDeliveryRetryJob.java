package com.byteCore.demo.service;


import com.byteCore.demo.domain.DeliveryLogEntity;
import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.enums.DeliveryStatus;
import com.byteCore.demo.repository.DeliveryLogRepository;
import com.byteCore.demo.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledDeliveryRetryJob {

    private final DeliveryLogRepository deliveryLogRepository;
    private final DigitalProductDeliveryService deliveryService;
    private final OrderRepository orderRepository;


    @Scheduled(cron = "0 */30 * * * *") // every 30 minutes
    public void retryFailedDeliveries() {

        log.info("Iniciando retry de entregas com falha...");


        List<DeliveryLogEntity> logsToRetry = deliveryLogRepository
                .findByStatusAndNextRetryAtBefore(
                        DeliveryStatus.FAILED,
                        Instant.now()
                );


        log.info("Entregas para retentar: {}", logsToRetry.size());


        for (DeliveryLogEntity deliveryLog : logsToRetry) {
            OrderEntity order = deliveryLog.getOrder();

            if (order == null) {
                log.warn("DeliveryLog ID {} não possui um pedido associado. Pulando...", deliveryLog.getId());
                continue;
            }

            deliveryLog.setStatus(DeliveryStatus.PENDING_RETRY);
            deliveryLog.setNextRetryAt(Instant.now().plus(30,
                    ChronoUnit.MINUTES));

            deliveryLogRepository.save(deliveryLog);


            try {
                deliveryService.deliverOrder(order);
                log.info("Retry bem-sucedido para pedido #{}", order.getId());
            }catch (Exception e){

                deliveryLog.setAttemptCount(deliveryLog.getAttemptCount() + 1);
                deliveryLog.setStatus(DeliveryStatus.FAILED);
                deliveryLog.setErrorMessage(e.getMessage());
                deliveryLog.setNextRetryAt(Instant.now().plus(30, ChronoUnit.MINUTES));

                deliveryLogRepository.save(deliveryLog);

                log.warn("Retry falhou para pedido #{}, Tentativa: {}",
                        order.getId(), deliveryLog.getAttemptCount());
            }

        }
        log.info("Retry jog concluido");
    }

}
