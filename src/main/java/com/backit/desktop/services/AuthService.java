package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.exceptions.AutenticacionException;
import com.backit.desktop.repositories.RolPermisoRepository;
import com.backit.desktop.repositories.UsuarioRepository;
import com.backit.desktop.security.EstadoUsuario;
import com.backit.desktop.security.SesionActual;
import model.seguridad.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Autenticación por correo + contraseña BCrypt.
 * - Límite de intentos: tras 5 fallos seguidos la cuenta queda en pausa 15 min (en memoria).
 *   Es distinto del "bloqueo administrativo" (estado BLOQUEADO), que solo cambia un administrador;
 *   así un atacante no puede dejar fuera de forma permanente al único SUPERADMIN.
 */
@Service
public class AuthService {

    static final int MAX_INTENTOS = 5;
    static final Duration PAUSA = Duration.ofMinutes(15);
    private static final String MENSAJE_CREDENCIALES = "Correo o contraseña incorrectos.";

    private record Intentos(int fallos, Instant pausadoHasta) {
    }

    private final UsuarioRepository usuarios;
    private final RolPermisoRepository rolPermisos;
    private final PasswordEncoder encoder;
    private final SesionActual sesion;
    private final AuditoriaService auditoria;
    private final Map<String, Intentos> intentos = new ConcurrentHashMap<>();
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarios, RolPermisoRepository rolPermisos, PasswordEncoder encoder,
                       SesionActual sesion, AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.rolPermisos = rolPermisos;
        this.encoder = encoder;
        this.sesion = sesion;
        this.auditoria = auditoria;
        this.hashFicticio = encoder.encode("hash-ficticio-para-igualar-tiempos");
    }

    public Usuario autenticar(String correo, String password) {
        String clave = correo == null ? "" : correo.trim().toLowerCase();
        String pass = password == null ? "" : password;

        Intentos actual = intentos.get(clave);
        if (actual != null && actual.pausadoHasta() != null && Instant.now().isBefore(actual.pausadoHasta())) {
            throw new AutenticacionException("Demasiados intentos fallidos. Espere unos minutos e intente de nuevo.");
        }

        Optional<Usuario> encontrado = usuarios.findByCorreoIgnoreCase(clave);
        if (encontrado.isEmpty()) {
            // Mismo costo de CPU que un usuario real: no revela qué correos existen.
            encoder.matches(pass, hashFicticio);
            registrarFallo(clave, null);
            throw new AutenticacionException(MENSAJE_CREDENCIALES);
        }

        Usuario u = encontrado.get();
        if (!encoder.matches(pass, u.getPasswordHash())) {
            registrarFallo(clave, u);
            throw new AutenticacionException(MENSAJE_CREDENCIALES);
        }
        if (!EstadoUsuario.ACTIVO.name().equals(u.getEstado())) {
            auditoria.registrar(u, "LOGIN_RECHAZADO", "usuario", u.getId(), Map.of("estado", u.getEstado()));
            throw new AutenticacionException("La cuenta no está activa. Contacte al administrador.");
        }

        intentos.remove(clave);
        sesion.iniciar(u, new HashSet<>(rolPermisos.findCodigosActivosByRolId(u.getRol().getId())));
        auditoria.registrar(u, "LOGIN_OK", "usuario", u.getId(), Map.of());
        return u;
    }

    public void cerrarSesion() {
        sesion.usuario().ifPresent(u ->
                auditoria.registrar(u, "LOGOUT", "usuario", u.getId(), Map.of()));
        sesion.cerrar();
    }

    private void registrarFallo(String clave, Usuario u) {
        Intentos previo = intentos.getOrDefault(clave, new Intentos(0, null));
        int fallos = previo.fallos() + 1;
        boolean pausar = fallos >= MAX_INTENTOS;
        intentos.put(clave, new Intentos(pausar ? 0 : fallos, pausar ? Instant.now().plus(PAUSA) : null));
        if (u != null) {
            auditoria.registrar(u, pausar ? "LOGIN_PAUSADO" : "LOGIN_FALLIDO", "usuario", u.getId(),
                    Map.of("fallos", fallos));
        }
    }
}
