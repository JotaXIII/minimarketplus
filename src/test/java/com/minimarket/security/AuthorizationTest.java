package com.minimarket.security;

import com.minimarket.entity.Venta;
import com.minimarket.service.UsuarioService;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de autorizacion (RBAC) sobre los endpoints criticos del backend,
 * usando Spring Security Test (@WithMockUser) y MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VentaService ventaService;

    @MockBean
    private UsuarioService usuarioService;

    private static final String VENTA_JSON = "{\"usuario\":{\"id\":1},\"detalles\":[]}";

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeRegistrarVenta_debeRetornar403() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VENTA_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void empleadoPuedeRegistrarVenta_debePermitirAcceso() throws Exception {
        when(ventaService.registrarVenta(any(Venta.class))).thenReturn(new Venta());

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VENTA_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void gerentePuedeRegistrarVenta_debePermitirAcceso() throws Exception {
        when(ventaService.registrarVenta(any(Venta.class))).thenReturn(new Venta());

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VENTA_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPuedeAdministrarUsuarios_debePermitirAcceso() throws Exception {
        when(usuarioService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioSinAutenticar_debeRechazarAcceso() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VENTA_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 401 || status == 302 || status == 403,
                "Se esperaba 401, 403 o redireccion (302) para un usuario no autenticado, status fue: " + status);
    }
}
