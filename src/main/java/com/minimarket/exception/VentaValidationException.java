package com.minimarket.exception;

/**
 * Excepcion lanzada cuando una {@link com.minimarket.entity.Venta} no cumple
 * las reglas de negocio requeridas para ser registrada (usuario invalido,
 * productos inexistentes, stock insuficiente o cantidades invalidas).
 */
public class VentaValidationException extends RuntimeException {

    public VentaValidationException(String message) {
        super(message);
    }
}
