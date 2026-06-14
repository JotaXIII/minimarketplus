package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.UsuarioValidationException;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.UsuarioService;
import com.minimarket.util.SanitizadorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Set<String> ROLES_VENTA_PERMITIDOS = Set.of("ADMIN", "GERENTE", "EMPLEADO");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public Usuario save(Usuario usuario) {
        // =========================
        // Sanitizacion de datos de entrada
        // =========================
        usuario.setUsername(SanitizadorUtil.limpiarTexto(usuario.getUsername()));
        usuario.setNombre(SanitizadorUtil.limpiarTexto(usuario.getNombre()));
        usuario.setApellido(SanitizadorUtil.limpiarTexto(usuario.getApellido()));
        usuario.setDireccion(SanitizadorUtil.limpiarTexto(usuario.getDireccion()));

        if (StringUtils.hasText(usuario.getEmail()) && !emailValido(usuario.getEmail())) {
            throw new UsuarioValidationException("El formato del email no es valido");
        }

        // =========================
        // Registro y persistencia
        // =========================
        log.info("Guardando usuario con username='{}'", usuario.getUsername());
        return usuarioRepository.save(usuario);
    }

    @Override
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public boolean tieneDatosCompletos(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        return StringUtils.hasText(usuario.getNombre())
                && StringUtils.hasText(usuario.getApellido())
                && StringUtils.hasText(usuario.getDireccion())
                && emailValido(usuario.getEmail());
    }

    @Override
    public boolean emailValido(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    @Override
    public boolean tieneRolPermitidoParaRegistrarVenta(Usuario usuario) {
        if (usuario == null || usuario.getRoles() == null) {
            return false;
        }
        return usuario.getRoles().stream()
                .map(Rol::getNombre)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .anyMatch(ROLES_VENTA_PERMITIDOS::contains);
    }
}
