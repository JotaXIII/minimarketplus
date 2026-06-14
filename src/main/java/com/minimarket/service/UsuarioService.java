package com.minimarket.service;

import com.minimarket.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> findAll();
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByUsername(String username);
    Usuario save(Usuario usuario);
    void deleteById(Long id);

    /**
     * Valida que el usuario tenga completos los datos obligatorios:
     * nombre, apellido, email (con formato valido) y direccion.
     */
    boolean tieneDatosCompletos(Usuario usuario);

    /**
     * Valida el formato basico de un email (usuario@dominio.tld).
     */
    boolean emailValido(String email);

    /**
     * Indica si el usuario posee un rol habilitado para registrar ventas
     * (ROLE_EMPLEADO, ROLE_GERENTE o ROLE_ADMIN).
     */
    boolean tieneRolPermitidoParaRegistrarVenta(Usuario usuario);
}
