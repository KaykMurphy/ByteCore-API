package com.byteCore.demo;

import com.byteCore.demo.domain.OrderEntity;
import com.byteCore.demo.domain.PaymentEntity;
import com.byteCore.demo.domain.ProductEntity;
import com.byteCore.demo.domain.UserEntity;
import com.byteCore.demo.enums.PaymentStatus;
import com.byteCore.demo.repository.PaymentRepository;
import com.byteCore.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledPaymentReleaseJob {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 3 * * *") // Roda às 03:00
    @Transactional
    public void releasePayments() {

        log.info("Iniciando job de liberação de pagamentos...");

        List<PaymentEntity> pendingReleases = paymentRepository.findByStatusAndMoneyReleasedFalseAndMoneyReleaseDateBefore(
                PaymentStatus.APPROVED,
                Instant.now()
        );

        log.info("Pagamentos a liberar: {}", pendingReleases.size());

        for (PaymentEntity payment : pendingReleases) {

            OrderEntity order = payment.getOrder();

            if (order == null) {
                log.warn("Pagamento ID {} não possui pedido associado. Pulando...", payment.getId());
                continue;
            }

            if (order.getItems().isEmpty()) {
                log.warn("Pedido #{} associado ao pagamento ID {} não possui itens. Pulando liberação.", order.getId(), payment.getId());
                continue;
            }

            ProductEntity product = order.getItems().get(0).getProduct();
            UserEntity seller = product.getSeller();

            if (seller == null) {
                log.warn("Impossível liberar pagamento ID {}: Vendedor não encontrado para o produto '{}'.", payment.getId(), product.getTitle());
                continue;
            }

            if (payment.getSellerAmount() == null) {
                log.warn("Pagamento ID {} aprovado sem valor de vendedor (sellerAmount) definido. Pulando liberação.", payment.getId());
                continue;
            }

            try {
                seller.movePendingToAvailable(payment.getSellerAmount());
                payment.markAsReleased();

                userRepository.save(seller);
                paymentRepository.save(payment);

                log.info("Pagamento {} liberado para vendedor {}", payment.getId(), seller.getName());

            } catch (Exception e) {
                log.error("Erro catastrófico ao liberar pagamento ID {}. Pulando para o próximo...", payment.getId(), e);
            }
        }

        log.info("Job concluído.");
    }
}