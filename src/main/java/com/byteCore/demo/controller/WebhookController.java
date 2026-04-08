package com.byteCore.demo.controller;

import com.byteCore.demo.dto.response.MercadoPagoWebhookDTO;
import com.byteCore.demo.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleMercadoPagoNotification(
            @RequestBody MercadoPagoWebhookDTO request,
            @RequestHeader("x-signature") String xSignature,
            @RequestHeader("x-request-id") String xRequestId) {

        log.info("Recebida notificação do Mercado Pago: Action={}, ID={}",
                request.getAction(), request.getId());

        if (request.getData() == null || request.getData().getId() == null) {
            log.warn("Corpo da notificação incompleto ou inválido.");
            return ResponseEntity.badRequest().build();
        }

        String resourceId = request.getData().getId();

//        if (!webhookService.validateSignature(xSignature, xRequestId, resourceId)) {
//            log.warn("ASSINATURA INVÁLIDA detectada para o recurso: {}. Acesso negado.", resourceId);
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
//        }

        webhookService.handleNotification(
                request.getAction(),
                "payment",
                resourceId
        );

        return ResponseEntity.ok().build();
    }
}