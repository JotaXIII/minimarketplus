package com.minimarket.service;

import com.minimarket.entity.Venta;

import java.util.List;

public interface VentaService {
    List<Venta> findAll();
    Venta findById(Long id);
    Venta save(Venta venta);
    List<Venta> findByUsuarioId(Long usuarioId);

    /**
     * Registra una venta validando usuario, productos, stock y cantidades.
     * El total se calcula a partir de los productos/detalles, sin confiar
     * en el valor de total recibido desde la peticion.
     *
     * @throws com.minimarket.exception.VentaValidationException si la venta
     *         no tiene usuario asociado valido, no tiene productos, alguna
     *         cantidad es invalida, algun producto no existe o no hay stock
     *         suficiente.
     */
    Venta registrarVenta(Venta venta);

    /**
     * Calcula el total de una venta sumando precio de producto x cantidad
     * de cada detalle.
     */
    double calcularTotal(Venta venta);
}
