package com.byteCore.demo;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class DocumentHashService {

    public String generateHash(String rawValue) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(encodedhash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao inicializar o algoritmo de hash SHA-256", e);
        }
    }
}