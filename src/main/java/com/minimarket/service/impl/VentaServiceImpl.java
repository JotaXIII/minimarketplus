package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.exception.VentaValidationException;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.UsuarioService;
import com.minimarket.service.VentaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta findById(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public Venta save(Venta venta) {
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @PreAuthorize("hasAnyRole('EMPLEADO','GERENTE','ADMIN')")
    public Venta registrarVenta(Venta venta) {
        if (venta == null) {
            throw new VentaValidationException("La venta no puede ser nula");
        }

        Usuario usuario = validarUsuarioDeVenta(venta);

        List<DetalleVenta> detalles = venta.getDetalles();
        if (detalles == null || detalles.isEmpty()) {
            throw new VentaValidationException("La venta debe tener al menos un producto");
        }

        double total = 0.0;
        for (DetalleVenta detalle : detalles) {
            total += procesarDetalle(detalle, venta);
        }

        venta.setUsuario(usuario);
        venta.setTotal(total);
        if (venta.getFecha() == null) {
            venta.setFecha(new Date());
        }

        log.info("Registrando venta para usuario id={} con total={}", usuario.getId(), total);
        return ventaRepository.save(venta);
    }

    private Usuario validarUsuarioDeVenta(Venta venta) {
        Usuario usuario = venta.getUsuario();
        if (usuario == null || usuario.getId() == null) {
            throw new VentaValidationException("La venta debe tener un usuario asociado valido");
        }

        Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new VentaValidationException("El usuario asociado a la venta no existe"));

        if (!usuarioService.tieneRolPermitidoParaRegistrarVenta(usuarioExistente)) {
            throw new VentaValidationException("El usuario no tiene un rol permitido para registrar ventas");
        }

        return usuarioExistente;
    }

    private double procesarDetalle(DetalleVenta detalle, Venta venta) {
        if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
            throw new VentaValidationException("La cantidad de un producto debe ser mayor a cero");
        }

        Producto productoDetalle = detalle.getProducto();
        if (productoDetalle == null || productoDetalle.getId() == null) {
            throw new VentaValidationException("La venta contiene un producto invalido");
        }

        Producto producto = productoRepository.findById(productoDetalle.getId())
                .orElseThrow(() -> new VentaValidationException("El producto asociado a la venta no existe"));

        if (producto.getStock() == null || producto.getStock() < detalle.getCantidad()) {
            throw new VentaValidationException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        producto.setStock(producto.getStock() - detalle.getCantidad());
        productoRepository.save(producto);
        registrarMovimientoInventario(producto, detalle.getCantidad());

        detalle.setProducto(producto);
        detalle.setPrecio(producto.getPrecio());
        detalle.setVenta(venta);

        return producto.getPrecio() * detalle.getCantidad();
    }

    private void registrarMovimientoInventario(Producto producto, Integer cantidad) {
        Inventario movimiento = new Inventario();
        movimiento.setProducto(producto);
        movimiento.setCantidad(cantidad);
        movimiento.setTipoMovimiento("Salida");
        movimiento.setFechaMovimiento(new Date());
        inventarioRepository.save(movimiento);
    }

    @Override
    public double calcularTotal(Venta venta) {
        if (venta == null || venta.getDetalles() == null) {
            return 0.0;
        }
        double total = 0.0;
        for (DetalleVenta detalle : venta.getDetalles()) {
            if (detalle.getProducto() != null && detalle.getProducto().getPrecio() != null
                    && detalle.getCantidad() != null) {
                total += detalle.getProducto().getPrecio() * detalle.getCantidad();
            }
        }
        return total;
    }
}
