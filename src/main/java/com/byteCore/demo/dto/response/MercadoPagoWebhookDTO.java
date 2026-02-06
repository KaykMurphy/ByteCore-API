package com.byteCore.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MercadoPagoWebhookDTO {

    // Tipo da notificação
    private String action;

    @JsonProperty("date_created")
    private String dateCreated;


     //ID único da notificação
    private Long id;

    private WebhookData data;

    @Data
    public static class WebhookData {
        private String id;
    }
}