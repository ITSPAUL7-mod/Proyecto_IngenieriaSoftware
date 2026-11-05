package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.CrearUsuarioDto;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.InstitucionRepository;
import com.backit.desktop.repositories.RolRepository;
import com.backit.desktop.repositories.UsuarioRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.EstadoUsuario;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.RolNombre;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.utils.Validadores;
import model.seguridad.Institucion;
import model.seguridad.Rol;
import model.seguridad.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aprovisionamiento de cuentas (Sprint 2).
 *
 * Jerarquía de altas:
 *   SUPERADMIN  → SUPERADMIN, AUDITOR (globales) y ADMIN_SEDE / PERSONAL_CUSTODIA / USUARIO_FINAL (de una institución)
 *   ADMIN_SEDE  → PERSONAL_CUSTODIA y USUARIO_FINAL, siempre dentro de SU institución
 *   resto       → ninguno
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final InstitucionRepository instituciones;
    private final PasswordEncoder encoder;
    private final SesionActual sesion;
    private final AutorizacionService autorizacion;
    private final AuditoriaService auditoria;

    public UsuarioService(UsuarioRepository usuarios, RolRepository roles, InstitucionRepository instituciones,
                          PasswordEncoder encoder, SesionActual sesion, AutorizacionService autorizacion,
                          AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.instituciones = instituciones;
        this.encoder = encoder;
        this.sesion = sesion;
        this.autorizacion = autorizacion;
        this.auditoria = auditoria;
    }

    @Transactional
    public Usuario crear(CrearUsuarioDto dto) {
        if (dto == null || dto.rol() == null) {
            throw new ValidacionException("Debe seleccionar un rol.");
        }
        Usuario actor = sesion.requerirUsuario();
        RolNombre actorRol = sesion.rolActual();
        RolNombre destino = dto.rol();

        // 1) ¿Tiene el permiso? 2) ¿Le corresponde crear ESE rol? (anti-escalamiento de privilegios)
        autorizacion.requerir(destino.esPrivilegiado()
                ? PermisoCodigo.USUARIO_CREAR_PRIVILEGIADO : PermisoCodigo.USUARIO_CREAR_LOCAL);
        if (!actorRol.puedeCrear(destino)) {
            throw new AccesoDenegadoException("Su perfil no puede crear cuentas de tipo " + destino + ".");
        }

        Institucion institucion = resolverInstitucion(actor, actorRol, destino, dto.institucionId());

        String nombre = Validadores.texto(dto.nombre(), "Nombres", 100);
        String apellido = Validadores.texto(dto.apellido(), "Apellidos", 100);
        String correo = Validadores.correo(dto.correo());
        Validadores.password(dto.password());
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new ValidacionException("Ya existe una cuenta con ese correo.");
        }

        Rol rol = roles.findByNombre(destino.name())
                .orElseThrow(() -> new IllegalStateException("Rol no sembrado: " + destino));

        OffsetDateTime ahora = OffsetDateTime.now();
        Usuario u = new Usuario();
        u.setRol(rol);
        u.setInstitucion(institucion);
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(dto.password()));
        u.setEstado(EstadoUsuario.ACTIVO.name());
        u.setCreadoEn(ahora);
        u.setActualizadoEn(ahora);
        usuarios.save(u);

        Map<String, Object> detalles = new LinkedHashMap<>();
        detalles.put("rol", destino.name());
        detalles.put("correo", correo);
        detalles.put("institucionId", institucion == null ? null : institucion.getId());
        auditoria.registrar(actor, "USUARIO_CREAR", "usuario", u.getId(), detalles);
        return u;
    }

    @Transactional
    public Usuario cambiarEstado(Long usuarioId, EstadoUsuario nuevoEstado) {
        autorizacion.requerir(PermisoCodigo.USUARIO_CAMBIAR_ESTADO);
        Usuario actor = sesion.requerirUsuario();
        if (nuevoEstado == null) {
            throw new ValidacionException("Debe seleccionar un estado.");
        }
        Usuario objetivo = usuarios.findById(usuarioId)
                .orElseThrow(() -> new ValidacionException("El usuario no existe."));
        if (objetivo.getId().equals(actor.getId())) {
            throw new ValidacionException("No puede cambiar el estado de su propia cuenta.");
        }
        if (!puedeGestionar(actor, sesion.rolActual(), objetivo)) {
            throw new AccesoDenegadoException("No puede gestionar esa cuenta.");
        }
        String anterior = objetivo.getEstado();
        objetivo.setEstado(nuevoEstado.name());
        objetivo.setActualizadoEn(OffsetDateTime.now());
        usuarios.save(objetivo);
        auditoria.registrar(actor, "USUARIO_CAMBIAR_ESTADO", "usuario", objetivo.getId(),
                Map.of("anterior", anterior, "nuevo", nuevoEstado.name()));
        return objetivo;
    }

    /** Cuentas globales (SUPERADMIN, AUDITOR) ven todo; ADMIN_SEDE solo su institución. */
    @Transactional(readOnly = true)
    public List<Usuario> listarVisibles() {
        autorizacion.requerir(PermisoCodigo.USUARIO_VER);
        Usuario actor = sesion.requerirUsuario();
        if (actor.getInstitucion() == null) {
            return usuarios.findAllByOrderByApellidoAscNombreAsc();
        }
        return usuarios.findByInstitucionIdOrderByApellidoAscNombreAsc(actor.getInstitucion().getId());
    }

    /** Roles que el usuario actual puede ofrecer en el formulario de alta (solo ayuda a la UI). */
    public List<RolNombre> rolesCreables() {
        return sesion.rolActual().rolesCreables();
    }

    private Institucion resolverInstitucion(Usuario actor, RolNombre actorRol, RolNombre destino, Long institucionId) {
        if (destino.esGlobal()) {
            if (institucionId != null) {
                throw new ValidacionException("Las cuentas " + destino + " son globales y no pertenecen a una institución.");
            }
            return null;
        }
        if (actorRol == RolNombre.ADMIN_SEDE) {
            Institucion propia = actor.getInstitucion();
            if (propia == null) {
                throw new AccesoDenegadoException("Su cuenta no tiene institución asignada.");
            }
            if (institucionId != null && !institucionId.equals(propia.getId())) {
                throw new AccesoDenegadoException("No puede crear cuentas fuera de su institución.");
            }
            return propia;
        }
        if (institucionId == null) {
            throw new ValidacionException("Debe seleccionar la institución de la cuenta.");
        }
        return instituciones.findById(institucionId)
                .filter(i -> Boolean.TRUE.equals(i.getActivo()))
                .orElseThrow(() -> new ValidacionException("La institución no existe o está inactiva."));
    }

    private boolean puedeGestionar(Usuario actor, RolNombre actorRol, Usuario objetivo) {
        if (actorRol == RolNombre.SUPERADMIN) {
            return true;
        }
        if (actorRol == RolNombre.ADMIN_SEDE) {
            Institucion propia = actor.getInstitucion();
            Institucion otra = objetivo.getInstitucion();
            return propia != null && otra != null && propia.getId().equals(otra.getId())
                    && actorRol.puedeCrear(RolNombre.desde(objetivo.getRol().getNombre()));
        }
        return false;
    }
}
