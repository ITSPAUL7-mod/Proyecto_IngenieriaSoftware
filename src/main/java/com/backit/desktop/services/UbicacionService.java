package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.CrearUbicacionDto;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.InstitucionRepository;
import com.backit.desktop.repositories.PuntoEntregaRepository;
import com.backit.desktop.repositories.UbicacionRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.utils.Validadores;
import model.operativo.Ubicacion;
import model.reclamacion.PuntoEntrega;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Ubicacion de sedes, edificios, pisos, zonas y bodegas
@Service
public class UbicacionService {

    private static final Set<String> TIPOS = Set.of("SEDE", "EDIFICIO", "PISO", "ZONA", "BODEGA");

    private final UbicacionRepository ubicaciones;
    private final PuntoEntregaRepository puntosEntrega;
    private final InstitucionRepository instituciones;
    private final AutorizacionService autorizacion;
    private final SesionActual sesion;
    private final AuditoriaService auditoria;

    public UbicacionService(UbicacionRepository ubicaciones, PuntoEntregaRepository puntosEntrega,
                            InstitucionRepository instituciones, AutorizacionService autorizacion,
                            SesionActual sesion, AuditoriaService auditoria) {
        this.ubicaciones = ubicaciones;
        this.puntosEntrega = puntosEntrega;
        this.instituciones = instituciones;
        this.autorizacion = autorizacion;
        this.sesion = sesion;
        this.auditoria = auditoria;
    }

    @Transactional
    public Ubicacion crear(CrearUbicacionDto dto) {
        autorizacion.requerir(PermisoCodigo.UBICACION_GESTIONAR);
        Usuario actor = sesion.requerirUsuario();
        // Una cuenta global (SUPERADMIN/AUDITOR) no tiene sede propia: debe indicar
        // explicitamente sobre cual institucion trabaja. Una cuenta local siempre usa la suya.
        Institucion institucion = resolverInstitucion(actor, dto.institucionId());

        String nombre = Validadores.texto(dto.nombre(), "Nombre de la ubicación", 120);
        String tipo = Validadores.texto(dto.tipo(), "Tipo de ubicación", 30).toUpperCase();
        if (!TIPOS.contains(tipo)) {
            throw new ValidacionException("Tipo no válido. Use SEDE, EDIFICIO, PISO, ZONA o BODEGA.");
        }
        if (ubicaciones.existsByInstitucionIdAndNombreIgnoreCase(institucion.getId(), nombre)) {
            throw new ValidacionException("Ya existe una ubicación con ese nombre en su institución.");
        }

        Ubicacion padre = null;
        if (dto.ubicacionPadreId() != null) {
            padre = ubicaciones.findById(dto.ubicacionPadreId())
                    .orElseThrow(() -> new ValidacionException("La ubicación padre no existe."));
            if (!padre.getInstitucion().getId().equals(institucion.getId())) {
                throw new AccesoDenegadoException("La ubicación padre pertenece a otra institución.");
            }
        }

        OffsetDateTime ahora = OffsetDateTime.now();
        Ubicacion u = new Ubicacion();
        u.setInstitucion(institucion);
        u.setUbicacionPadre(padre);
        u.setNombre(nombre);
        u.setTipo(tipo);
        u.setDireccion(opcional(dto.direccion(), "Dirección", 250));
        u.setReferencia(opcional(dto.referencia(), "Referencia", 250));
        u.setActivo(true);
        u.setCreadoEn(ahora);
        ubicaciones.save(u);

        boolean conPunto = dto.horarioAtencion() != null && !dto.horarioAtencion().isBlank();
        if (conPunto) {
            String horario = Validadores.texto(dto.horarioAtencion(), "Horario de atención", 150);
            PuntoEntrega punto = new PuntoEntrega();
            punto.setInstitucion(institucion);
            punto.setUbicacion(u);
            punto.setNombrePunto(nombre + " - Punto de entrega");
            punto.setHorarioAtencion(horario);
            punto.setActivo(true);
            punto.setCreadoEn(ahora);
            puntosEntrega.save(punto);
        }

        auditoria.registrar(actor, "UBICACION_CREAR", "ubicacion", u.getId(),
                Map.of("nombre", nombre, "tipo", tipo, "puntoEntrega", conPunto,
                        "institucionId", institucion.getId()));
        return u;
    }

    @Transactional(readOnly = true)
    public List<Ubicacion> visibles() {
        return visiblesDe(null);
    }

    @Transactional(readOnly = true)
    public List<Ubicacion> visiblesDe(Long institucionId) {
        Usuario actor = sesion.requerirUsuario();
        if (actor.getInstitucion() != null) {
            if (institucionId != null && !institucionId.equals(actor.getInstitucion().getId())) {
                throw new AccesoDenegadoException("No puede consultar ubicaciones de otra institución.");
            }
            return ubicaciones.findByInstitucionIdOrderByNombreAsc(actor.getInstitucion().getId());
        }
        if (institucionId == null) {
            return ubicaciones.findAllByOrderByNombreAsc();
        }
        return ubicaciones.findByInstitucionIdOrderByNombreAsc(institucionId);
    }

    private Institucion resolverInstitucion(Usuario actor, Long institucionId) {
        if (actor.getInstitucion() != null) {
            if (institucionId != null && !institucionId.equals(actor.getInstitucion().getId())) {
                throw new AccesoDenegadoException("No puede crear ubicaciones en otra institución.");
            }
            return actor.getInstitucion();
        }
        if (institucionId == null) {
            throw new ValidacionException("Su cuenta es global: seleccione la institución de la ubicación.");
        }
        return instituciones.findById(institucionId)
                .filter(i -> Boolean.TRUE.equals(i.getActivo()))
                .orElseThrow(() -> new ValidacionException("La institución no existe o está inactiva."));
    }

    private static String opcional(String valor, String campo, int max) {
        return valor == null || valor.isBlank() ? null : Validadores.texto(valor, campo, max);
    }
}
