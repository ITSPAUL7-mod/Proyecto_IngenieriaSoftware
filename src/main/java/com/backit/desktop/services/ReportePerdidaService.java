package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.BuscarReportesDto;
import com.backit.desktop.dto.CrearReporteDto;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.matching.MotorCoincidenciasService;
import com.backit.desktop.repositories.CategoriaObjetoRepository;
import com.backit.desktop.repositories.InstitucionRepository;
import com.backit.desktop.repositories.ReportePerdidaRepository;
import com.backit.desktop.repositories.UbicacionRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.utils.Validadores;
import model.operativo.CategoriaObjeto;
import model.operativo.ReportePerdida;
import model.operativo.Ubicacion;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Sprint 4 - reportes de perdida y busqueda por atributos. */
@Service
public class ReportePerdidaService {

    private static final int DESCRIPCION_MAX = 4000;

    private final ReportePerdidaRepository reportes;
    private final CategoriaObjetoRepository categorias;
    private final UbicacionRepository ubicaciones;
    private final InstitucionRepository instituciones;
    private final MotorCoincidenciasService motor;
    private final AutorizacionService autorizacion;
    private final SesionActual sesion;
    private final AuditoriaService auditoria;

    public ReportePerdidaService(ReportePerdidaRepository reportes, CategoriaObjetoRepository categorias,
                                 UbicacionRepository ubicaciones, InstitucionRepository instituciones,
                                 MotorCoincidenciasService motor, AutorizacionService autorizacion,
                                 SesionActual sesion, AuditoriaService auditoria) {
        this.reportes = reportes;
        this.categorias = categorias;
        this.ubicaciones = ubicaciones;
        this.instituciones = instituciones;
        this.motor = motor;
        this.autorizacion = autorizacion;
        this.sesion = sesion;
        this.auditoria = auditoria;
    }

    @Transactional
    public ReportePerdida crear(CrearReporteDto dto) {
        autorizacion.requerir(PermisoCodigo.REPORTE_PERDIDA_CREAR);
        Usuario actor = sesion.requerirUsuario();
        Institucion institucion = resolverInstitucion(actor, dto.institucionId());

        String titulo = Validadores.texto(dto.titulo(), "Título", 150);
        String descripcion = Validadores.texto(dto.descripcion(), "Descripción", DESCRIPCION_MAX);
        String privadas = dto.caracteristicasPrivadas() == null || dto.caracteristicasPrivadas().isBlank()
                ? null : Validadores.texto(dto.caracteristicasPrivadas(), "Señas reservadas", DESCRIPCION_MAX);

        CategoriaObjeto categoria = categorias.findById(dto.categoriaId())
                .orElseThrow(() -> new ValidacionException("Seleccione una categoría válida."));
        Ubicacion ubicacion = null;
        if (dto.ubicacionId() != null) {
            ubicacion = ubicaciones.findById(dto.ubicacionId())
                    .orElseThrow(() -> new ValidacionException("La ubicación indicada no existe."));
            if (!ubicacion.getInstitucion().getId().equals(institucion.getId())) {
                throw new AccesoDenegadoException("La ubicación pertenece a otra institución.");
            }
        }
        OffsetDateTime fecha = dto.fechaPerdida() == null
                ? OffsetDateTime.now()
                : dto.fechaPerdida().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();

        OffsetDateTime ahora = OffsetDateTime.now();
        ReportePerdida r = new ReportePerdida();
        r.setInstitucion(institucion);
        r.setUsuario(actor);
        r.setCategoria(categoria);
        r.setUbicacionPerdida(ubicacion);
        r.setTitulo(titulo);
        r.setDescripcion(descripcion);
        r.setCaracteristicasPrivadas(privadas);
        r.setFechaPerdida(fecha);
        r.setEstado("ACTIVO");
        r.setCreadoEn(ahora);
        r.setActualizadoEn(ahora);
        reportes.save(r);

        auditoria.registrar(actor, "REPORTE_CREAR", "reporte_perdida", r.getId(),
                Map.of("titulo", titulo, "categoria", categoria.getNombre(), "institucionId", institucion.getId()));

        // Sprint 5: comparar con los hallazgos en custodia.
        motor.evaluarNuevoReporte(r);
        return r;
    }

    /** Busqueda por texto, categoria, rango de fechas y estado, limitada a la institucion visible. */
    @Transactional(readOnly = true)
    public List<ReportePerdida> buscar(BuscarReportesDto filtros) {
        sesion.requerirUsuario();
        List<ReportePerdida> origen = visibles();
        if (filtros == null) {
            return origen;
        }

        String texto = filtros.texto() == null ? "" : filtros.texto().trim().toLowerCase(Locale.ROOT);
        return origen.stream()
                .filter(r -> texto.isBlank()
                        || r.getTitulo().toLowerCase(Locale.ROOT).contains(texto)
                        || r.getDescripcion().toLowerCase(Locale.ROOT).contains(texto))
                .filter(r -> filtros.categoriaId() == null
                        || (r.getCategoria() != null && filtros.categoriaId().equals(r.getCategoria().getId())))
                .filter(r -> filtros.fechaDesde() == null || fecha(r).isEqual(filtros.fechaDesde())
                        || fecha(r).isAfter(filtros.fechaDesde()))
                .filter(r -> filtros.fechaHasta() == null || fecha(r).isEqual(filtros.fechaHasta())
                        || fecha(r).isBefore(filtros.fechaHasta()))
                .filter(r -> filtros.estado() == null || filtros.estado().isBlank()
                        || filtros.estado().equalsIgnoreCase(r.getEstado()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportePerdida> listarPropios() {
        return reportes.findByUsuarioIdOrderByCreadoEnDesc(sesion.requerirUsuario().getId());
    }

    @Transactional(readOnly = true)
    public List<ReportePerdida> visibles() {
        Usuario actor = sesion.requerirUsuario();
        if (actor.getInstitucion() == null) {
            return reportes.findAllByOrderByCreadoEnDesc();
        }
        return reportes.findByInstitucionIdOrderByCreadoEnDesc(actor.getInstitucion().getId());
    }

    private static LocalDate fecha(ReportePerdida r) {
        return r.getFechaPerdida() == null ? LocalDate.MIN : r.getFechaPerdida().toLocalDate();
    }

    private Institucion resolverInstitucion(Usuario actor, Long institucionId) {
        if (actor.getInstitucion() != null) {
            return actor.getInstitucion();
        }
        if (institucionId == null) {
            throw new ValidacionException("Su cuenta es global: debe seleccionar la institución del reporte.");
        }
        return instituciones.findById(institucionId)
                .filter(i -> Boolean.TRUE.equals(i.getActivo()))
                .orElseThrow(() -> new ValidacionException("La institución no existe o está inactiva."));
    }
}
