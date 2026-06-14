package com.minimarket.service;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.exception.VentaValidationException;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio de Venta.
 * Cubren validacion de usuario, productos, stock y cantidades, calculo del
 * total y relaciones Venta-Usuario / Venta-DetalleVenta-Producto / Producto-Inventario,
 * usando Mockito para simular UsuarioRepository, ProductoRepository,
 * VentaRepository, InventarioRepository y UsuarioService.
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Usuario usuario;
    private Producto producto;
    private DetalleVenta detalle;
    private Venta venta;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("empleado1");
        usuario.setRoles(Set.of(new Rol("EMPLEADO")));

        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Pan");
        producto.setPrecio(1000.0);
        producto.setStock(5);

        detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(2);

        venta = new Venta();
        venta.setUsuario(usuario);
        venta.setDetalles(new ArrayList<>(java.util.List.of(detalle)));
    }

    @Test
    void registrarVenta_conStockSuficiente_debeGuardarVenta() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneRolPermitidoParaRegistrarVenta(usuario)).thenReturn(true);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta resultado = ventaService.registrarVenta(venta);

        assertNotNull(resultado);
        assertEquals(2000.0, resultado.getTotal());
        assertEquals(3, producto.getStock(), "El stock debe descontarse segun la cantidad vendida");
        verify(productoRepository).save(producto);
        verify(inventarioRepository).save(any(Inventario.class));
        verify(ventaRepository).save(venta);
    }

    @Test
    void registrarVenta_sinStockSuficiente_debeLanzarExcepcion() {
        producto.setStock(1);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneRolPermitidoParaRegistrarVenta(usuario)).thenReturn(true);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThrows(VentaValidationException.class, () -> ventaService.registrarVenta(venta));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void calcularTotalVenta_debeSumarCorrectamenteLosProductos() {
        Producto producto2 = new Producto();
        producto2.setId(20L);
        producto2.setPrecio(500.0);

        DetalleVenta detalle2 = new DetalleVenta();
        detalle2.setProducto(producto2);
        detalle2.setCantidad(3);

        venta.getDetalles().add(detalle2);

        double total = ventaService.calcularTotal(venta);

        assertEquals(3500.0, total);
    }

    @Test
    void registrarVenta_conUsuarioValido_debeAsociarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneRolPermitidoParaRegistrarVenta(usuario)).thenReturn(true);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta resultado = ventaService.registrarVenta(venta);

        assertNotNull(resultado.getUsuario());
        assertEquals(usuario.getId(), resultado.getUsuario().getId());
    }

    @Test
    void registrarVenta_sinUsuario_debeLanzarExcepcion() {
        venta.setUsuario(null);

        assertThrows(VentaValidationException.class, () -> ventaService.registrarVenta(venta));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conCantidadInvalida_debeLanzarExcepcion() {
        detalle.setCantidad(0);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneRolPermitidoParaRegistrarVenta(usuario)).thenReturn(true);

        assertThrows(VentaValidationException.class, () -> ventaService.registrarVenta(venta));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conProductoInexistente_debeLanzarExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneRolPermitidoParaRegistrarVenta(usuario)).thenReturn(true);
        when(productoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(VentaValidationException.class, () -> ventaService.registrarVenta(venta));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conRolNoPermitido_debeLanzarExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneRolPermitidoParaRegistrarVenta(usuario)).thenReturn(false);

        assertThrows(VentaValidationException.class, () -> ventaService.registrarVenta(venta));
        verify(productoRepository, never()).findById(any());
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conCantidadNegativa_debeLanzarExcepcion() {
        detalle.setCantidad(-1);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneRolPermitidoParaRegistrarVenta(usuario)).thenReturn(true);

        assertThrows(VentaValidationException.class, () -> ventaService.registrarVenta(venta));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conCantidadCero_debeLanzarExcepcion() {
        detalle.setCantidad(0);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneRolPermitidoParaRegistrarVenta(usuario)).thenReturn(true);

        assertThrows(VentaValidationException.class, () -> ventaService.registrarVenta(venta));
        verify(ventaRepository, never()).save(any(Venta.class));
    }
}
