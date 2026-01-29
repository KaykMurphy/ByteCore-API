package com.byteCore.demo.exceptions;

import java.time.Instant;
import java.util.Map;

public record ValidationError(

        Instant timestamp,
        Integer status,
        String error,
        Map<String, String> errors,
        String path

) {}
