package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.CoincidenciaDto;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.CoincidenciaRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.RolNombre;
import com.backit.desktop.security.SesionActual;
import model.operativo.Coincidencia;
import model.seguridad.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/** Sprint 5 - consulta y revision humana de coincidencias sugeridas. */
@Service
public class CoincidenciaService {

    private final CoincidenciaRepository coincidencias;
    private final AutorizacionService autorizacion;
    private final SesionActual sesion;
    private final AuditoriaService auditoria;

    public CoincidenciaService(CoincidenciaRepository coincidencias, AutorizacionService autorizacion,
                               SesionActual sesion, AuditoriaService auditoria) {
        this.coincidencias = coincidencias;
        this.autorizacion = autorizacion;
        this.sesion = sesion;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<CoincidenciaDto> listarVisibles() {
        boolean revisar = sesion.tienePermiso(PermisoCodigo.COINCIDENCIA_REVISAR);
        boolean propias = sesion.tienePermiso(PermisoCodigo.COINCIDENCIA_VER_PROPIAS);
        if (!revisar && !propias) {
            throw new AccesoDenegadoException("No tiene permiso para consultar coincidencias.");
        }

        Usuario actor = sesion.requerirUsuario();
        List<Coincidencia> lista;
        if (sesion.rolActual() == RolNombre.USUARIO_FINAL) {
            lista = coincidencias.findByReportePerdidaUsuarioIdOrderByFechaCoincidenciaDesc(actor.getId());
        } else if (actor.getInstitucion() == null) {
            lista = coincidencias.findAllByOrderByFechaCoincidenciaDesc();
        } else {
            lista = coincidencias.findByObjetoEncontradoInstitucionIdOrderByFechaCoincidenciaDesc(
                    actor.getInstitucion().getId());
        }
        return lista.stream().map(this::aDto).toList();
    }

    @Transactional
    public void marcarRevisada(Long id, boolean aprobar) {
        autorizacion.requerir(PermisoCodigo.COINCIDENCIA_REVISAR);
        Coincidencia c = coincidencias.findById(id)
                .orElseThrow(() -> new ValidacionException("La coincidencia no existe."));
        Usuario actor = sesion.requerirUsuario();
        c.setEstado(aprobar ? "REVISADA" : "DESCARTADA");
        c.setVerificadoPor(actor);
        coincidencias.save(c);
        auditoria.registrar(actor, aprobar ? "COINCIDENCIA_REVISADA" : "COINCIDENCIA_DESCARTADA",
                "coincidencia", c.getId(), Map.of("total", c.getPuntajeTotal().toPlainString()));
    }

    private CoincidenciaDto aDto(Coincidencia c) {
        return new CoincidenciaDto(
                c.getId(),
                c.getObjetoEncontrado() == null ? "" : c.getObjetoEncontrado().getTitulo(),
                c.getReportePerdida() == null ? "" : c.getReportePerdida().getTitulo(),
                n(c.getPuntajeTotal()),
                c.getEstado(),
                n(c.getPuntajeCategoria()),
                n(c.getPuntajeDescripcion()),
                n(c.getPuntajeUbicacion()),
                n(c.getPuntajeFecha()),
                n(c.getPuntajeCaracteristicas()));
    }

    private static BigDecimal n(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
