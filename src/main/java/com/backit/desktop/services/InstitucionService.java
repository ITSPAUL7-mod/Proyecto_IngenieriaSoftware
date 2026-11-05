package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.CrearInstitucionDto;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.InstitucionRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.RolNombre;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.utils.Validadores;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class InstitucionService {

    private final InstitucionRepository instituciones;
    private final AutorizacionService autorizacion;
    private final SesionActual sesion;
    private final AuditoriaService auditoria;

    public InstitucionService(InstitucionRepository instituciones, AutorizacionService autorizacion,
                              SesionActual sesion, AuditoriaService auditoria) {
        this.instituciones = instituciones;
        this.autorizacion = autorizacion;
        this.sesion = sesion;
        this.auditoria = auditoria;
    }

    @Transactional
    public Institucion crear(CrearInstitucionDto dto) {
        autorizacion.requerir(PermisoCodigo.INSTITUCION_GESTIONAR);
        Usuario actor = sesion.requerirUsuario();
        Institucion i = construir(dto);
        instituciones.save(i);
        auditoria.registrar(actor, "INSTITUCION_CREAR", "institucion", i.getId(), Map.of("nombre", i.getNombre()));
        return i;
    }

    /**
     * Uso exclusivo del asistente de primer arranque, cuando aún no existe ningún usuario que pueda autenticarse.
     * NO debe invocarse desde la UI: solo la llama ConfiguracionInicialService, que antes verifica que
     * no exista ningún SUPERADMIN. (Es public porque un método package-private no se intercepta de
     * forma fiable en el proxy transaccional.)
     */
    @Transactional
    public Institucion crearSinSesion(CrearInstitucionDto dto) {
        Institucion i = construir(dto);
        return instituciones.save(i);
    }

    /** SUPERADMIN ve todas las activas; ADMIN_SEDE solo la propia. */
    @Transactional(readOnly = true)
    public List<Institucion> visibles() {
        Usuario actor = sesion.requerirUsuario();
        if (sesion.rolActual() == RolNombre.SUPERADMIN || actor.getInstitucion() == null) {
            return instituciones.findByActivoTrueOrderByNombreAsc();
        }
        return List.of(actor.getInstitucion());
    }

    private Institucion construir(CrearInstitucionDto dto) {
        String nombre = Validadores.texto(dto.nombre(), "Nombre de la institución", 150);
        if (instituciones.existsByNombreIgnoreCase(nombre)) {
            throw new ValidacionException("Ya existe una institución con ese nombre.");
        }
        String ruc = dto.ruc() == null || dto.ruc().isBlank() ? null : Validadores.texto(dto.ruc(), "RUC", 20);
        if (ruc != null && instituciones.existsByRuc(ruc)) {
            throw new ValidacionException("Ya existe una institución con ese RUC.");
        }
        Institucion i = new Institucion();
        i.setNombre(nombre);
        i.setRuc(ruc);
        i.setDireccion(opcional(dto.direccion(), "Dirección", 250));
        i.setTelefono(opcional(dto.telefono(), "Teléfono", 30));
        i.setContactoPrincipal(opcional(dto.contactoPrincipal(), "Contacto principal", 150));
        i.setActivo(true);
        i.setCreadoEn(OffsetDateTime.now());
        return i;
    }

    private static String opcional(String valor, String campo, int max) {
        return valor == null || valor.isBlank() ? null : Validadores.texto(valor, campo, max);
    }
}
