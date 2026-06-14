package com.minimarket;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    public void testCrearUsuario() {
        Set<Rol> roles = Set.of(new Rol("ADMIN"));

        Usuario usuario = new Usuario();
        usuario.setUsername("adminUser");
        usuario.setPassword("securePassword123");
        usuario.setRoles(roles);

        assertNotNull(usuario);
        assertEquals("adminUser", usuario.getUsername());
        assertEquals("securePassword123", usuario.getPassword());
        assertEquals(1, usuario.getRoles().size());
        assertTrue(usuario.getRoles().stream().anyMatch(role -> role.getNombre().equals("ADMIN")));
    }

    @Test
    public void testEquals() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setUsername("adminUser");
        usuario1.setPassword("securePassword123");

        Usuario usuario2 = new Usuario();
        usuario2.setId(1L);
        usuario2.setUsername("adminUser");
        usuario2.setPassword("securePassword123");

        assertEquals(usuario1.getId(), usuario2.getId());
        assertEquals(usuario1.getUsername(), usuario2.getUsername());
        assertEquals(usuario1.getPassword(), usuario2.getPassword());
    }

    @Test
    public void testAgregarRoles() {
        Usuario usuario = new Usuario();
        usuario.setUsername("user1");
        usuario.setPassword("password");

        Rol roleUser = new Rol("USER");
        Rol roleAdmin = new Rol("ADMIN");
        usuario.setRoles(Set.of(roleUser, roleAdmin));

        assertEquals(2, usuario.getRoles().size());
        assertTrue(usuario.getRoles().stream().anyMatch(role -> role.getNombre().equals("USER")));
        assertTrue(usuario.getRoles().stream().anyMatch(role -> role.getNombre().equals("ADMIN")));
    }
}
