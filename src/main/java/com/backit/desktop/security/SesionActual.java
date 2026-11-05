package com.backit.desktop.security;

import com.backit.desktop.exceptions.AutenticacionException;
import model.seguridad.Usuario;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Sesión del único usuario que usa la aplicación de escritorio.
 * Los servicios leen de aquí el actor; nunca lo reciben desde la UI (evita suplantación por parámetro).
 */
@Component
public class SesionActual {

    private volatile Usuario usuario;
    private volatile Set<String> permisos = Set.of();

    public void iniciar(Usuario usuario, Set<String> codigosPermiso) {
        this.permisos = Set.copyOf(codigosPermiso);
        this.usuario = usuario;
    }

    public void cerrar() {
        this.usuario = null;
        this.permisos = Set.of();
    }

    public Optional<Usuario> usuario() {
        return Optional.ofNullable(usuario);
    }

    public Usuario requerirUsuario() {
        Usuario u = usuario;
        if (u == null) {
            throw new AutenticacionException("No hay una sesión iniciada.");
        }
        return u;
    }

    public RolNombre rolActual() {
        return RolNombre.desde(requerirUsuario().getRol().getNombre());
    }

    public boolean tienePermiso(PermisoCodigo permiso) {
        return usuario != null && permisos.contains(permiso.name());
    }
}
