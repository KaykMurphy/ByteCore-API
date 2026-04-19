package com.byteCore.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class ChatbotService {

    private final RestClient restClient;

    public ChatbotService() {
        this.restClient = RestClient.create("http://localhost:5000");
    }

    public String conversarComBot(String mensagemUsuario) {

        Map<String, String> requestBody = Map.of("mensagem", mensagemUsuario);

        Map response = restClient.post()
                .uri("/chat")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        return (String) response.get("resposta");
    }
}