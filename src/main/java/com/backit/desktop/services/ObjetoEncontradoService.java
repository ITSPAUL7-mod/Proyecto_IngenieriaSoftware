package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.RegistrarObjetoDto;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.matching.MotorCoincidenciasService;
import com.backit.desktop.qr.QrService;
import com.backit.desktop.repositories.CategoriaObjetoRepository;
import com.backit.desktop.repositories.InstitucionRepository;
import com.backit.desktop.repositories.ObjetoEncontradoRepository;
import com.backit.desktop.repositories.UbicacionRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.storage.AlmacenamientoService;
import com.backit.desktop.utils.Validadores;
import model.operativo.CategoriaObjeto;
import model.operativo.ObjetoEncontrado;
import model.operativo.Ubicacion;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//registro, foto protegida y QR de los diferentes objetos encontrados
@Service
public class ObjetoEncontradoService {

    private static final int DESCRIPCION_MAX = 4000;

    private final ObjetoEncontradoRepository objetos;
    private final CategoriaObjetoRepository categorias;
    private final UbicacionRepository ubicaciones;
    private final InstitucionRepository instituciones;
    private final AlmacenamientoService almacenamiento;
    private final QrService qr;
    private final MotorCoincidenciasService motor;
    private final AutorizacionService autorizacion;
    private final SesionActual sesion;
    private final AuditoriaService auditoria;

    public ObjetoEncontradoService(ObjetoEncontradoRepository objetos, CategoriaObjetoRepository categorias,
                                   UbicacionRepository ubicaciones, InstitucionRepository instituciones,
                                   AlmacenamientoService almacenamiento, QrService qr,
                                   MotorCoincidenciasService motor, AutorizacionService autorizacion,
                                   SesionActual sesion, AuditoriaService auditoria) {
        this.objetos = objetos;
        this.categorias = categorias;
        this.ubicaciones = ubicaciones;
        this.instituciones = instituciones;
        this.almacenamiento = almacenamiento;
        this.qr = qr;
        this.motor = motor;
        this.autorizacion = autorizacion;
        this.sesion = sesion;
        this.auditoria = auditoria;
    }

    @Transactional
    public ObjetoEncontrado registrar(RegistrarObjetoDto dto, byte[] foto, String nombreFoto) {
        autorizacion.requerir(PermisoCodigo.OBJETO_REGISTRAR);
        Usuario actor = sesion.requerirUsuario();
        Institucion institucion = resolverInstitucion(actor, dto.institucionId());

        String titulo = Validadores.texto(dto.titulo(), "Título", 150);
        String descripcion = Validadores.texto(dto.descripcion(), "Descripción", DESCRIPCION_MAX);
        String privadas = dto.caracteristicasPrivadas() == null || dto.caracteristicasPrivadas().isBlank()
                ? null : Validadores.texto(dto.caracteristicasPrivadas(), "Características privadas", DESCRIPCION_MAX);

        CategoriaObjeto categoria = categorias.findById(dto.categoriaId())
                .orElseThrow(() -> new ValidacionException("Seleccione una categoría válida."));
        if (!Boolean.TRUE.equals(categoria.getActivo())) {
            throw new ValidacionException("La categoría seleccionada está inactiva.");
        }
        Ubicacion ubicacion = ubicaciones.findById(dto.ubicacionId())
                .orElseThrow(() -> new ValidacionException("Seleccione una ubicación válida."));
        if (!ubicacion.getInstitucion().getId().equals(institucion.getId())) {
            throw new AccesoDenegadoException("La ubicación pertenece a otra institución.");
        }

        OffsetDateTime fecha = dto.fechaEncontrado() == null
                ? OffsetDateTime.now()
                : dto.fechaEncontrado().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();

        OffsetDateTime ahora = OffsetDateTime.now();
        ObjetoEncontrado objeto = new ObjetoEncontrado();
        objeto.setInstitucion(institucion);
        objeto.setCategoria(categoria);
        objeto.setUbicacion(ubicacion);
        objeto.setEncontradoPor(actor);
        objeto.setTitulo(titulo);
        objeto.setDescripcion(descripcion);
        objeto.setCaracteristicasPrivadas(privadas);
        objeto.setFechaEncontrado(fecha);
        objeto.setEstado("REGISTRADO");
        objeto.setCodigoQr(UUID.randomUUID());
        objeto.setCreadoEn(ahora);

        if (foto != null && foto.length > 0) {
            try {
                AlmacenamientoService.ArchivoGuardado guardado =
                        almacenamiento.guardarFoto(foto, nombreFoto, "objeto");
                objeto.setFotoRuta(guardado.ruta());
                objeto.setFotoHash(guardado.hash());
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo guardar la foto del hallazgo.", e);
            }
        }

        objetos.save(objeto);
        auditoria.registrar(actor, "OBJETO_REGISTRAR", "objeto_encontrado", objeto.getId(),
                Map.of("titulo", titulo, "categoria", categoria.getNombre(),
                        "codigoQr", objeto.getCodigoQr().toString(), "institucionId", institucion.getId()));

        // Sprint 5: cada hallazgo se compara con los reportes activos.
        motor.evaluarNuevoObjeto(objeto);
        return objeto;
    }

    @Transactional(readOnly = true)
    public List<ObjetoEncontrado> listarVisibles() {
        autorizacion.requerir(PermisoCodigo.OBJETO_VER);
        Usuario actor = sesion.requerirUsuario();
        if (actor.getInstitucion() == null) {
            return objetos.findAllByOrderByCreadoEnDesc();
        }
        return objetos.findByInstitucionIdOrderByCreadoEnDesc(actor.getInstitucion().getId());
    }

    /** Genera la imagen PNG del QR del objeto; el token es el unico contenido. */
    // No es readOnly: ademas de generar la imagen se inserta la auditoria OBJETO_QR_GENERADO.
    @Transactional
    public byte[] etiquetaQr(Long objetoId) {
        autorizacion.requerir(PermisoCodigo.OBJETO_VER);
        ObjetoEncontrado objeto = objetos.findById(objetoId)
                .orElseThrow(() -> new ValidacionException("El objeto no existe."));
        verificarVisibilidad(objeto);
        try {
            byte[] png = qr.generarPng(objeto.getCodigoQr(), 320);
            auditoria.registrar(sesion.requerirUsuario(), "OBJETO_QR_GENERADO", "objeto_encontrado",
                    objeto.getId(), Map.of("codigoQr", objeto.getCodigoQr().toString()));
            return png;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar la imagen QR.", e);
        }
    }

    private void verificarVisibilidad(ObjetoEncontrado objeto) {
        Usuario actor = sesion.requerirUsuario();
        if (actor.getInstitucion() != null
                && !actor.getInstitucion().getId().equals(objeto.getInstitucion().getId())) {
            throw new AccesoDenegadoException("El objeto pertenece a otra institución.");
        }
    }

    private Institucion resolverInstitucion(Usuario actor, Long institucionId) {
        if (actor.getInstitucion() != null) {
            return actor.getInstitucion();
        }
        if (institucionId == null) {
            throw new ValidacionException("Su cuenta es global: debe seleccionar la institución del hallazgo.");
        }
        return instituciones.findById(institucionId)
                .filter(i -> Boolean.TRUE.equals(i.getActivo()))
                .orElseThrow(() -> new ValidacionException("La institución no existe o está inactiva."));
    }
}
