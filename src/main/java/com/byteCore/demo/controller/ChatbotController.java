package com.byteCore.demo.controller;

import com.byteCore.demo.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/perguntar")
    public ResponseEntity<Map<String, String>> perguntar(@RequestBody Map<String, String> payload) {
        String mensagem = payload.get("mensagem");
        String resposta = chatbotService.conversarComBot(mensagem);

        return ResponseEntity.ok(Map.of("resposta", resposta));
    }
}