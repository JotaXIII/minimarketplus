package com.minimarket.exception;

/**
 * Excepcion lanzada cuando los datos de un {@link com.minimarket.entity.Usuario}
 * no cumplen las reglas de validacion de negocio (datos obligatorios, formato
 * de email o rol requerido para una operacion critica).
 */
public class UsuarioValidationException extends RuntimeException {

    public UsuarioValidationException(String message) {
        super(message);
    }
}
