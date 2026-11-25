package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.CrearReclamacionDto;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.CoincidenciaRepository;
import com.backit.desktop.repositories.EvidenciaDevolucionRepository;
import com.backit.desktop.repositories.ObjetoEncontradoRepository;
import com.backit.desktop.repositories.ReclamacionRepository;
import com.backit.desktop.repositories.UsuarioRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.RolNombre;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.storage.AlmacenamientoService;
import com.backit.desktop.utils.Hashing;
import com.backit.desktop.utils.Validadores;
import model.operativo.Coincidencia;
import model.operativo.ObjetoEncontrado;
import model.reclamacion.EvidenciaDevolucion;
import model.reclamacion.Reclamacion;
import model.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ReclamacionService {

    private static final Set<String> ESTADOS_RECLAMABLES = Set.of("SUGERIDA", "REVISADA");

    private final ReclamacionRepository reclamaciones;
    private final EvidenciaDevolucionRepository evidencias;
    private final CoincidenciaRepository coincidencias;
    private final ObjetoEncontradoRepository objetos;
    private final UsuarioRepository usuarios;
    private final AlmacenamientoService almacenamiento;
    private final NotificacionService notificaciones;
    private final AutorizacionService autorizacion;
    private final SesionActual sesion;
    private final AuditoriaService auditoria;

    public ReclamacionService(ReclamacionRepository reclamaciones,
                              EvidenciaDevolucionRepository evidencias,
                              CoincidenciaRepository coincidencias,
                              ObjetoEncontradoRepository objetos,
                              UsuarioRepository usuarios,
                              AlmacenamientoService almacenamiento,
                              NotificacionService notificaciones,
                              AutorizacionService autorizacion,
                              SesionActual sesion,
                              AuditoriaService auditoria) {
        this.reclamaciones = reclamaciones;
        this.evidencias = evidencias;
        this.coincidencias = coincidencias;
        this.objetos = objetos;
        this.usuarios = usuarios;
        this.almacenamiento = almacenamiento;
        this.notificaciones = notificaciones;
        this.autorizacion = autorizacion;
        this.sesion = sesion;
        this.auditoria = auditoria;
    }

    /** El dueno del reporte inicia la reclamacion; admite una primera evidencia opcional. */
    @Transactional
    public Reclamacion crear(CrearReclamacionDto dto, byte[] evidencia, String nombreArchivo,
                             String tipoEvidencia, String descripcionEvidencia) {
        autorizacion.requerir(PermisoCodigo.RECLAMACION_CREAR);
        Usuario actor = sesion.requerirUsuario();

        Coincidencia c = coincidencias.findById(dto.coincidenciaId())
                .orElseThrow(() -> new ValidacionException("La coincidencia no existe."));
        if (c.getReportePerdida() == null || !actor.getId().equals(c.getReportePerdida().getUsuario().getId())) {
            throw new AccesoDenegadoException("Solo el dueño del reporte puede reclamar este objeto.");
        }
        if (!ESTADOS_RECLAMABLES.contains(c.getEstado())) {
            throw new ValidacionException("Esa coincidencia ya no admite una reclamación nueva.");
        }
        if (reclamaciones.findByCoincidenciaId(c.getId()).isPresent()) {
            throw new ValidacionException("Ya existe una reclamación para esa coincidencia.");
        }

        OffsetDateTime ahora = OffsetDateTime.now();
        Reclamacion r = new Reclamacion();
        r.setCoincidencia(c);
        r.setUsuarioSolicitante(actor);
        r.setEstado("PENDIENTE");
        r.setFechaSolicitud(ahora);
        r.setCreadoEn(ahora);
        // Prueba no reversible de la verificacion de identidad (sin guardar el documento).
        r.setDocumentoVerificadoHash(Hashing.sha256Hex(
                actor.getId() + "|" + actor.getCorreo() + "|" + ahora.toInstant().toString()));
        reclamaciones.save(r);

        if (evidencia != null && evidencia.length > 0) {
            guardarEvidencia(r, evidencia, nombreArchivo, tipoEvidencia, descripcionEvidencia, actor);
        }

        c.setEstado("RECLAMADA");
        coincidencias.save(c);

        auditoria.registrar(actor, "RECLAMACION_CREAR", "reclamacion", r.getId(),
                Map.of("coincidenciaId", c.getId(), "objetoId", c.getObjetoEncontrado().getId()));

        notificaciones.notificar(actor, "Reclamación registrada",
                "Su reclamación #" + r.getId() + " está PENDIENTE de verificación por el personal autorizado.",
                "RECLAMO");
        notificarResponsables(actor, "Nueva reclamación #" + r.getId()
                + " para el objeto «" + c.getObjetoEncontrado().getTitulo() + "».");
        return r;
    }

    @Transactional(readOnly = true)
    public List<Reclamacion> listarVisibles() {
        boolean crear = sesion.tienePermiso(PermisoCodigo.RECLAMACION_CREAR);
        boolean resolver = sesion.tienePermiso(PermisoCodigo.RECLAMACION_RESOLVER);
        if (!crear && !resolver) {
            throw new AccesoDenegadoException("No tiene permiso para consultar reclamaciones.");
        }
        Usuario actor = sesion.requerirUsuario();
        if (sesion.rolActual() == RolNombre.USUARIO_FINAL) {
            return reclamaciones.findByUsuarioSolicitanteIdOrderByFechaSolicitudDesc(actor.getId());
        }
        if (actor.getInstitucion() == null) {
            return reclamaciones.findAllByOrderByFechaSolicitudDesc();
        }
        return reclamaciones.findByCoincidenciaObjetoEncontradoInstitucionIdOrderByFechaSolicitudDesc(
                actor.getInstitucion().getId());
    }

    @Transactional
    public Reclamacion aprobar(Long id) {
        autorizacion.requerir(PermisoCodigo.RECLAMACION_RESOLVER);
        Reclamacion r = cargarPendiente(id);
        Usuario actor = sesion.requerirUsuario();
        r.setEstado("APROBADA");
        r.setAtendidoPor(actor);
        r.setFechaResolucion(OffsetDateTime.now());
        reclamaciones.save(r);

        ObjetoEncontrado objeto = r.getCoincidencia().getObjetoEncontrado();
        objeto.setEstado("RECLAMADO");
        objetos.save(objeto);

        auditoria.registrar(actor, "RECLAMACION_APROBADA", "reclamacion", r.getId(),
                Map.of("objetoId", objeto.getId()));
        notificaciones.notificar(r.getUsuarioSolicitante(), "Reclamación aprobada",
                "Su reclamación #" + r.getId() + " fue aprobada. Puede proceder a la entrega física.",
                "RECLAMO");
        return r;
    }

    @Transactional
    public Reclamacion rechazar(Long id, String motivo) {
        autorizacion.requerir(PermisoCodigo.RECLAMACION_RESOLVER);
        String razon = Validadores.texto(motivo, "Motivo del rechazo", 3000);
        Reclamacion r = cargarPendiente(id);
        Usuario actor = sesion.requerirUsuario();
        r.setEstado("RECHAZADA");
        r.setMotivoRechazo(razon);
        r.setAtendidoPor(actor);
        r.setFechaResolucion(OffsetDateTime.now());
        reclamaciones.save(r);

        auditoria.registrar(actor, "RECLAMACION_RECHAZADA", "reclamacion", r.getId(),
                Map.of("motivo", razon));
        notificaciones.notificar(r.getUsuarioSolicitante(), "Reclamación rechazada",
                "Su reclamación #" + r.getId() + " fue rechazada. Motivo: " + razon, "RECLAMO");
        return r;
    }

    @Transactional
    public EvidenciaDevolucion agregarEvidencia(Long reclamacionId, byte[] bytes, String nombreArchivo,
                                                String tipoEvidencia, String descripcion) {
        Reclamacion r = reclamaciones.findById(reclamacionId)
                .orElseThrow(() -> new ValidacionException("La reclamación no existe."));
        Usuario actor = sesion.requerirUsuario();
        boolean esDueno = actor.getId().equals(r.getUsuarioSolicitante().getId());
        if (!esDueno && !sesion.tienePermiso(PermisoCodigo.RECLAMACION_RESOLVER)) {
            throw new AccesoDenegadoException("No puede aportar evidencias a esta reclamación.");
        }
        return guardarEvidencia(r, bytes, nombreArchivo, tipoEvidencia, descripcion, actor);
    }

    @Transactional(readOnly = true)
    public List<EvidenciaDevolucion> evidenciasDe(Long reclamacionId) {
        return evidencias.findByReclamacionIdOrderByCreadoEnDesc(reclamacionId);
    }

    private Reclamacion cargarPendiente(Long id) {
        Reclamacion r = reclamaciones.findById(id)
                .orElseThrow(() -> new ValidacionException("La reclamación no existe."));
        if (!"PENDIENTE".equals(r.getEstado())) {
            throw new ValidacionException("La reclamación ya fue resuelta.");
        }
        return r;
    }

    private EvidenciaDevolucion guardarEvidencia(Reclamacion r, byte[] bytes, String nombreArchivo,
                                                  String tipoEvidencia, String descripcion, Usuario actor) {
        try {
            AlmacenamientoService.ArchivoGuardado guardado =
                    almacenamiento.guardarEvidencia(bytes, nombreArchivo, "evidencia");
            EvidenciaDevolucion e = new EvidenciaDevolucion();
            e.setReclamacion(r);
            e.setTipoEvidencia(tipo(tipoEvidencia));
            e.setRutaArchivo(guardado.ruta());
            e.setHashArchivo(guardado.hash());
            e.setDescripcion(descripcion == null || descripcion.isBlank() ? null :
                    Validadores.texto(descripcion, "Descripción de la evidencia", 300));
            e.setSubidoPor(actor);
            e.setClasificacion("SENSIBLE");
            e.setCreadoEn(OffsetDateTime.now());
            evidencias.save(e);
            auditoria.registrar(actor, "EVIDENCIA_AGREGADA", "evidencia_devolucion", e.getId(),
                    Map.of("reclamacionId", r.getId(), "tipo", e.getTipoEvidencia()));
            return e;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la evidencia.", ex);
        }
    }

    private void notificarResponsables(Usuario actor, String mensaje) {
        List<Usuario> responsables;
        if (actor.getInstitucion() == null) {
            responsables = usuarios.findByRolNombreOrderByApellidoAscNombreAsc(RolNombre.ADMIN_SEDE.name());
        } else {
            responsables = usuarios.findByRolNombreAndInstitucionIdOrderByApellidoAscNombreAsc(
                    RolNombre.ADMIN_SEDE.name(), actor.getInstitucion().getId());
        }
        for (Usuario responsable : responsables) {
            notificaciones.notificar(responsable, "Nueva reclamación pendiente", mensaje, "RECLAMO");
        }
    }

    private static String tipo(String valor) {
        if (valor == null || valor.isBlank()) {
            return "OTRA";
        }
        return switch (valor.toUpperCase(Locale.ROOT)) {
            case "FACTURA", "FACTURA DE COMPRA" -> "FACTURA";
            case "FOTO", "FOTOGRAFIA ADICIONAL", "FOTOGRAFÍA ADICIONAL" -> "FOTO";
            case "SEÑA", "SEÑA ESPECIFICA", "SEÑA ESPECÍFICA" -> "SEÑA";
            case "FIRMA", "FIRMA DIGITAL" -> "FIRMA";
            default -> "OTRA";
        };
    }
}
