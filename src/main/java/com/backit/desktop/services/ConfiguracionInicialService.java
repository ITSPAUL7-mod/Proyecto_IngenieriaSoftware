package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.ConfiguracionInicialDto;
import com.backit.desktop.dto.CrearInstitucionDto;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.RolRepository;
import com.backit.desktop.repositories.UsuarioRepository;
import com.backit.desktop.security.EstadoUsuario;
import com.backit.desktop.security.RolNombre;
import com.backit.desktop.utils.Validadores;
import model.seguridad.Institucion;
import model.seguridad.Rol;
import model.seguridad.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Creación del PRIMER SUPERADMIN (arranque en frío).
 *
 * No existe contraseña por defecto ni cuenta "admin/admin" en el código: el asistente solo se
 * ofrece mientras NO exista ningún SUPERADMIN en la base de datos y se cierra para siempre al crearlo.
 * Todos los SUPERADMIN posteriores se crean desde UsuarioService, con sesión y auditoría.
 */
@Service
public class ConfiguracionInicialService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final InstitucionService instituciones;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;

    public ConfiguracionInicialService(UsuarioRepository usuarios, RolRepository roles,
                                       InstitucionService instituciones, PasswordEncoder encoder,
                                       AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.instituciones = instituciones;
        this.encoder = encoder;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public boolean requiereConfiguracionInicial() {
        return !usuarios.existsByRolNombre(RolNombre.SUPERADMIN.name());
    }

    @Transactional
    public Usuario crearSuperAdminInicial(ConfiguracionInicialDto dto) {
        if (!requiereConfiguracionInicial()) {
            throw new AccesoDenegadoException("El sistema ya fue configurado.");
        }
        String nombre = Validadores.texto(dto.nombre(), "Nombres", 100);
        String apellido = Validadores.texto(dto.apellido(), "Apellidos", 100);
        String correo = Validadores.correo(dto.correo());
        Validadores.password(dto.password());
        if (!dto.password().equals(dto.passwordConfirmacion())) {
            throw new ValidacionException("Las contraseñas no coinciden.");
        }

        Rol rol = roles.findByNombre(RolNombre.SUPERADMIN.name())
                .orElseThrow(() -> new IllegalStateException("Roles no sembrados: falta SUPERADMIN."));

        Institucion institucion = instituciones.crearSinSesion(
                new CrearInstitucionDto(dto.nombreInstitucion(), null, null, null, null));

        OffsetDateTime ahora = OffsetDateTime.now();
        Usuario u = new Usuario();
        u.setRol(rol);
        u.setInstitucion(null); // cuenta global
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(dto.password()));
        u.setEstado(EstadoUsuario.ACTIVO.name());
        u.setCreadoEn(ahora);
        u.setActualizadoEn(ahora);
        usuarios.save(u);

        auditoria.registrar(u, "SUPERADMIN_INICIAL_CREADO", "usuario", u.getId(),
                Map.of("correo", correo, "institucionId", institucion.getId()));
        return u;
    }
}
