package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.UsuarioRepository;
import com.backit.desktop.utils.Hashing;
import com.backit.desktop.utils.Validadores;
import model.seguridad.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sprint 1 - recuperación controlada de cuenta.
 * No hay servicio de correo en el prototipo: el token se devuelve al llamador para
 * que la UI lo muestre. En produccion debe enviarse por un canal seguro.
 * Solo se guarda el HASH del token (nunca el token en claro) y caduca a los 30 min.
 */
@Service
public class RecuperacionService {

    private static final long VIGENCIA_MINUTOS = 30;

    private record Token(Long usuarioId, Instant expiraEn) {
    }

    private final UsuarioRepository usuarios;
    private final AuthService authService;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;
    private final Map<String, Token> tokens = new ConcurrentHashMap<>();

    public RecuperacionService(UsuarioRepository usuarios, AuthService authService,
                               PasswordEncoder encoder, AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.authService = authService;
        this.encoder = encoder;
        this.auditoria = auditoria;
    }

    /** Devuelve siempre un token (real o ficticio) para no revelar qué correos existen. */
    @Transactional
    public String solicitar(String correo) {
        String clave = correo == null ? "" : correo.trim().toLowerCase();
        String token = UUID.randomUUID().toString();

        Optional<Usuario> encontrado = usuarios.findByCorreoIgnoreCase(clave);
        if (encontrado.isEmpty()) {
            return token;
        }

        Usuario u = encontrado.get();
        tokens.put(Hashing.sha256Hex(token),
                new Token(u.getId(), Instant.now().plusSeconds(VIGENCIA_MINUTOS * 60)));
        auditoria.registrar(u, "RECUPERACION_SOLICITADA", "usuario", u.getId(),
                Map.of("correo", clave));
        return token;
    }

    /** Canjea el token por una nueva contraseña y limpia el bloqueo por intentos. */
    @Transactional
    public void restablecer(String token, String nuevaPassword) {
        if (token == null || token.isBlank()) {
            throw new ValidacionException("Debe indicar el código de recuperación.");
        }
        Token t = tokens.remove(Hashing.sha256Hex(token.trim()));
        if (t == null || Instant.now().isAfter(t.expiraEn())) {
            throw new ValidacionException("El código de recuperación no es válido o ha caducado.");
        }

        Usuario u = usuarios.findById(t.usuarioId())
                .orElseThrow(() -> new ValidacionException("La cuenta ya no existe."));
        Validadores.password(nuevaPassword);
        u.setPasswordHash(encoder.encode(nuevaPassword));
        u.setActualizadoEn(OffsetDateTime.now());
        usuarios.save(u);

        authService.desbloquear(u.getCorreo());
        auditoria.registrar(u, "RECUPERACION_COMPLETADA", "usuario", u.getId(), Map.of());
    }
}
