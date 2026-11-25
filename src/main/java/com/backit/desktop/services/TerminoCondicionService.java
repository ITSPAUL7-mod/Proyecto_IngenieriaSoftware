package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.CrearTerminoDto;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.AceptacionTerminosRepository;
import com.backit.desktop.repositories.TerminoCondicionRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.utils.Hashing;
import com.backit.desktop.utils.Origen;
import com.backit.desktop.utils.Validadores;
import model.seguridad.Usuario;
import model.sistema.AceptacionTerminos;
import model.sistema.TerminoCondicion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

//Terminos y Condiciones del aplicativo BackIt
@Service
public class TerminoCondicionService {

    private static final int CONTENIDO_MAX = 20_000;

    private final TerminoCondicionRepository terminos;
    private final AceptacionTerminosRepository aceptaciones;
    private final AutorizacionService autorizacion;
    private final SesionActual sesion;
    private final AuditoriaService auditoria;

    public TerminoCondicionService(TerminoCondicionRepository terminos,
                                   AceptacionTerminosRepository aceptaciones,
                                   AutorizacionService autorizacion,
                                   SesionActual sesion,
                                   AuditoriaService auditoria) {
        this.terminos = terminos;
        this.aceptaciones = aceptaciones;
        this.autorizacion = autorizacion;
        this.sesion = sesion;
        this.auditoria = auditoria;
    }

    /** Version vigente = activa con fecha de vigencia mas reciente. */
    @Transactional(readOnly = true)
    public TerminoCondicion vigente() {
        return terminos.findByActivoTrueOrderByFechaVigenciaDesc().stream()
                .findFirst()
                .orElseThrow(() -> new ValidacionException(
                        "No hay una versión vigente de Términos y Condiciones. Contacte al administrador."));
    }

    @Transactional(readOnly = true)
    public boolean requiereAceptacion(Usuario usuario) {
        if (usuario == null) {
            return true;
        }
        TerminoCondicion v = vigente();
        return !aceptaciones.existsByUsuarioIdAndTerminoCondicionIdAndAceptadoTrueAndRevocadoEnIsNull(
                usuario.getId(), v.getId());
    }


    @Transactional
    public void registrarDecision(Usuario usuario, boolean aceptado) {
        if (usuario == null) {
            throw new ValidacionException("Debe iniciar sesión para aceptar los términos.");
        }
        TerminoCondicion v = vigente();
        OffsetDateTime ahora = OffsetDateTime.now();

        aceptaciones.findTopByUsuarioIdAndTerminoCondicionIdOrderByFechaAceptacionDesc(
                        usuario.getId(), v.getId())
                .filter(a -> a.getRevocadoEn() == null)
                .ifPresent(a -> {
                    a.setRevocadoEn(ahora);
                    aceptaciones.save(a);
                });

        AceptacionTerminos registro = new AceptacionTerminos();
        registro.setUsuario(usuario);
        registro.setTerminoCondicion(v);
        registro.setFechaAceptacion(ahora);
        registro.setDispositivoHash(Hashing.sha256Hex(Origen.actual() + "|" + usuario.getId()));
        registro.setOrigen(Origen.actual());
        registro.setAceptado(aceptado);
        registro.setCreadoEn(ahora);
        aceptaciones.save(registro);

        auditoria.registrar(usuario, aceptado ? "TERMINOS_ACEPTAR" : "TERMINOS_RECHAZAR",
                "termino_condicion", v.getId(), Map.of("version", v.getVersion(),
                        "contenidoHash", v.getContenidoHash()));
    }

    @Transactional
    public TerminoCondicion publicar(CrearTerminoDto dto) {
        autorizacion.requerir(PermisoCodigo.TERMINOS_GESTIONAR);
        String version = Validadores.texto(dto.version(), "Versión", 30);
        String titulo = Validadores.texto(dto.titulo(), "Título", 150);
        String contenido = Validadores.texto(dto.contenido(), "Contenido", CONTENIDO_MAX);
        if (terminos.findByVersion(version).isPresent()) {
            throw new ValidacionException("Ya existe la versión " + version + ".");
        }

        terminos.findByActivoTrueOrderByFechaVigenciaDesc()
                .forEach(t -> {
                    t.setActivo(false);
                    terminos.save(t);
                });

        OffsetDateTime ahora = OffsetDateTime.now();
        TerminoCondicion t = new TerminoCondicion();
        t.setVersion(version);
        t.setTitulo(titulo);
        t.setContenido(contenido);
        t.setContenidoHash(Hashing.sha256Hex(contenido));
        t.setFechaVigencia(ahora);
        t.setActivo(true);
        t.setCreadoPor(sesion.requerirUsuario());
        t.setCreadoEn(ahora);
        terminos.save(t);

        auditoria.registrar(sesion.requerirUsuario(), "TERMINOS_PUBLICAR", "termino_condicion", t.getId(),
                Map.of("version", version, "contenidoHash", t.getContenidoHash()));
        // La sesion debe pedir otra aceptacion: se recalculan los permisos en el proximo login.
        return t;
    }
}
