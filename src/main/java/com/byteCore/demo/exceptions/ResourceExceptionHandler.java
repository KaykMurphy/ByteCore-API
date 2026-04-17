package com.byteCore.demo.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StandardError> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        StandardError error = new StandardError(
              Instant.now(),
              status.value(),
              "Not found",
              ex.getMessage(),
              request.getRequestURI()
                );


        return  ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        // mapa que vai guardar: campo -> mensagem de erro
        Map<String, String> errors = new HashMap<>();

        // pega todos os erros de validação dos campos
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        for (FieldError fieldError : fieldErrors) {
            String fieldName = fieldError.getField();
            String errorMessage = fieldError.getDefaultMessage();

            errors.put(fieldName, errorMessage);
        }

        ValidationError error = new ValidationError(
                Instant.now(),
                status.value(),
                "Validation error",
                errors,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<StandardError> handleDuplicateEmailException(
            DuplicateEmailException ex,
            HttpServletRequest request){

        HttpStatus status = HttpStatus.CONFLICT; // 409

        StandardError error = new StandardError(
                Instant.now(),
                status.value(),
                "Duplicate email",
                ex.getMessage(),
                request.getRequestURI()
        );

        log.warn("Duplicate email attempt: {}", ex.getMessage());
        return ResponseEntity.status(status).body(error);

    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<StandardError> handleInvalidCredentialsException(
            InvalidCredentialsException ex,
            HttpServletRequest request){

        HttpStatus status = HttpStatus.UNAUTHORIZED; // 401

        StandardError error = new StandardError(
                Instant.now(),
                status.value(),
                "Invalid credentials",
                ex.getMessage(),
                request.getRequestURI()
        );

        log.warn("Invalid credentials attempt: {}", ex.getMessage());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardError> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request){

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        StandardError error = new StandardError(
                Instant.now(),
                status.value(),
                "Bad credentials",
                ex.getMessage(),
                request.getRequestURI()
        );

        log.warn("Bad credentials attempt: {}", ex.getMessage());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardError> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandardError error = new StandardError(
                Instant.now(),
                status.value(),
                "Malformed JSON request",
                "Erro ao ler o corpo da requisição. Verifique se o formato e os tipos de dados (como números e datas) estão corretos.",
                request.getRequestURI()
        );

        log.warn("Malformed JSON attempt: {}", ex.getMessage());
        return ResponseEntity.status(status).body(error);
    }

}
