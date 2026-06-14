package com.minimarket.service;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.UsuarioValidationException;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio de Usuario.
 * Cubren validacion de datos obligatorios, formato de email, rol habilitado
 * para registrar ventas e interaccion simulada con UsuarioRepository (Mockito).
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioCompleto;

    @BeforeEach
    void setUp() {
        usuarioCompleto = new Usuario();
        usuarioCompleto.setUsername("jperez");
        usuarioCompleto.setPassword("ClaveSegura123");
        usuarioCompleto.setNombre("Juan");
        usuarioCompleto.setApellido("Perez");
        usuarioCompleto.setEmail("jperez@example.com");
        usuarioCompleto.setDireccion("Av. Siempre Viva 123");
    }

    @Test
    void usuarioConDatosCompletos_debeSerValido() {
        assertTrue(usuarioService.tieneDatosCompletos(usuarioCompleto));
    }

    @Test
    void usuarioSinEmail_debeSerInvalido() {
        usuarioCompleto.setEmail(null);
        assertFalse(usuarioService.tieneDatosCompletos(usuarioCompleto));
    }

    @Test
    void usuarioSinDireccion_debeSerInvalido() {
        usuarioCompleto.setDireccion(null);
        assertFalse(usuarioService.tieneDatosCompletos(usuarioCompleto));
    }

    @Test
    void usuarioConEmailMalFormado_debeSerInvalido() {
        usuarioCompleto.setEmail("correo-no-valido");
        assertFalse(usuarioService.tieneDatosCompletos(usuarioCompleto));
        assertFalse(usuarioService.emailValido("correo-no-valido"));
    }

    @Test
    void usuarioConRolPermitido_puedeRegistrarVenta() {
        usuarioCompleto.setRoles(Set.of(new Rol("EMPLEADO")));
        assertTrue(usuarioService.tieneRolPermitidoParaRegistrarVenta(usuarioCompleto));
    }

    @Test
    void usuarioConRolNoPermitido_noPuedeRegistrarVenta() {
        usuarioCompleto.setRoles(Set.of(new Rol("CLIENTE")));
        assertFalse(usuarioService.tieneRolPermitidoParaRegistrarVenta(usuarioCompleto));
    }

    @Test
    void guardarUsuario_debeSanitizarYDelegarEnRepositorio() {
        usuarioCompleto.setNombre("  Juan<script>  ");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.save(usuarioCompleto);

        assertEquals("Juanscript", guardado.getNombre());
        verify(usuarioRepository, times(1)).save(usuarioCompleto);
    }

    @Test
    void guardarUsuario_conEmailInvalido_debeLanzarExcepcion() {
        usuarioCompleto.setEmail("correo-no-valido");

        assertThrows(UsuarioValidationException.class, () -> usuarioService.save(usuarioCompleto));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void findById_debeRetornarUsuarioDesdeRepositorio() {
        usuarioCompleto.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(usuarioCompleto));

        assertTrue(usuarioService.findById(1L).isPresent());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void findByUsername_debeRetornarUsuarioDesdeRepositorio() {
        when(usuarioRepository.findByUsername("jperez")).thenReturn(java.util.Optional.of(usuarioCompleto));

        assertTrue(usuarioService.findByUsername("jperez").isPresent());
        verify(usuarioRepository, times(1)).findByUsername("jperez");
    }

    @Test
    void findAll_debeRetornarListaDesdeRepositorio() {
        when(usuarioRepository.findAll()).thenReturn(java.util.List.of(usuarioCompleto));

        assertEquals(1, usuarioService.findAll().size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void deleteById_debeInvocarRepositorio() {
        usuarioService.deleteById(1L);

        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void usuarioConEmailFormatoInvalido_debeSerInvalido() {
        usuarioCompleto.setEmail("juan.perez@dominio");

        assertFalse(usuarioService.emailValido(usuarioCompleto.getEmail()));
        assertFalse(usuarioService.tieneDatosCompletos(usuarioCompleto));
    }

    @Test
    void usuarioConNombrePotencialmentePeligroso_debeSerSanitizado() {
        usuarioCompleto.setNombre("<script>alert('xss')</script>Juan");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario guardado = usuarioService.save(usuarioCompleto);

        assertFalse(guardado.getNombre().contains("<"), "El nombre sanitizado no debe contener '<'");
        assertFalse(guardado.getNombre().contains(">"), "El nombre sanitizado no debe contener '>'");
    }

    @Test
    void usuarioSinRolPermitido_noPuedeRegistrarVenta() {
        usuarioCompleto.setRoles(Set.of());

        assertFalse(usuarioService.tieneRolPermitidoParaRegistrarVenta(usuarioCompleto));
    }
}
