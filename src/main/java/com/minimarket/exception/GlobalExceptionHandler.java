package com.minimarket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Traduce las excepciones de validacion de negocio a respuestas HTTP 400
 * con un cuerpo JSON uniforme, evitando exponer trazas internas o datos
 * sensibles al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UsuarioValidationException.class, VentaValidationException.class})
    public ResponseEntity<Map<String, String>> handleValidationException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
