package com.backit.desktop.matching;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.repositories.CoincidenciaRepository;
import com.backit.desktop.repositories.ObjetoEncontradoRepository;
import com.backit.desktop.repositories.ReportePerdidaRepository;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.services.NotificacionService;
import model.operativo.Coincidencia;
import model.operativo.ObjetoEncontrado;
import model.operativo.ReportePerdida;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class MotorCoincidenciasService {

    public static final Map<String, BigDecimal> PESOS = Map.of(
            "CATEGORIA", new BigDecimal("0.25"),
            "DESCRIPCION", new BigDecimal("0.30"),
            "UBICACION", new BigDecimal("0.20"),
            "FECHA", new BigDecimal("0.15"),
            "CARACTERISTICAS", new BigDecimal("0.10"));

    private static final List<String> ESTADOS_REPORTE_ACTIVOS = List.of("ACTIVO", "COINCIDENCIA");
    private static final List<String> ESTADOS_OBJETO_CANDIDATOS = List.of("REGISTRADO", "EN_CUSTODIA");

    private final List<CriterioMatching> criterios;
    private final ReportePerdidaRepository reportes;
    private final ObjetoEncontradoRepository objetos;
    private final CoincidenciaRepository coincidencias;
    private final NotificacionService notificaciones;
    private final AuditoriaService auditoria;
    private final SesionActual sesion;
    private final BigDecimal umbral;

    public MotorCoincidenciasService(List<CriterioMatching> criterios,
                                     ReportePerdidaRepository reportes,
                                     ObjetoEncontradoRepository objetos,
                                     CoincidenciaRepository coincidencias,
                                     NotificacionService notificaciones,
                                     AuditoriaService auditoria,
                                     SesionActual sesion,
                                     @Value("${backit.matching.umbral:70}") BigDecimal umbral) {
        this.criterios = criterios;
        this.reportes = reportes;
        this.objetos = objetos;
        this.coincidencias = coincidencias;
        this.notificaciones = notificaciones;
        this.auditoria = auditoria;
        this.sesion = sesion;
        this.umbral = umbral;
    }

    public ResultadoMatching calcular(ObjetoEncontrado o, ReportePerdida r) {
        Map<String, BigDecimal> puntos = new HashMap<>();
        for (CriterioMatching criterio : criterios) {
            puntos.put(criterio.nombre(), criterio.puntaje(o, r));
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> peso : PESOS.entrySet()) {
            total = total.add(puntos.getOrDefault(peso.getKey(), BigDecimal.ZERO).multiply(peso.getValue()));
        }
        return new ResultadoMatching(
                puntos.getOrDefault("CATEGORIA", BigDecimal.ZERO),
                puntos.getOrDefault("DESCRIPCION", BigDecimal.ZERO),
                puntos.getOrDefault("UBICACION", BigDecimal.ZERO),
                puntos.getOrDefault("FECHA", BigDecimal.ZERO),
                puntos.getOrDefault("CARACTERISTICAS", BigDecimal.ZERO),
                total.setScale(2, RoundingMode.HALF_UP));
    }

    @Transactional
    public void evaluarNuevoObjeto(ObjetoEncontrado objeto) {
        for (ReportePerdida reporte : reportes.findByEstadoIn(ESTADOS_REPORTE_ACTIVOS)) {
            if (!mismaInstitucion(objeto.getInstitucion(), reporte.getInstitucion())) {
                continue;
            }
            evaluarPar(objeto, reporte);
        }
    }

    @Transactional
    public void evaluarNuevoReporte(ReportePerdida reporte) {
        Long institucionId = reporte.getInstitucion() == null ? null : reporte.getInstitucion().getId();
        if (institucionId == null) {
            return;
        }
        for (ObjetoEncontrado objeto : objetos.findByEstadoInAndInstitucionId(
                ESTADOS_OBJETO_CANDIDATOS, institucionId)) {
            evaluarPar(objeto, reporte);
        }
    }

    @Transactional
    public ResultadoRecalculo recalcularTodo() {
        var candidatosObjetos = objetos.findByEstadoIn(ESTADOS_OBJETO_CANDIDATOS);
        var candidatosReportes = reportes.findByEstadoIn(ESTADOS_REPORTE_ACTIVOS);
        int pares = 0;
        int creadas = 0;
        for (ObjetoEncontrado objeto : candidatosObjetos) {
            for (ReportePerdida reporte : candidatosReportes) {
                if (!mismaInstitucion(objeto.getInstitucion(), reporte.getInstitucion())) {
                    continue;
                }
                if (coincidencias.existsByObjetoEncontradoIdAndReportePerdidaId(objeto.getId(), reporte.getId())) {
                    continue;
                }
                pares++;
                ResultadoMatching resultado = calcular(objeto, reporte);
                if (resultado.total().compareTo(umbral) >= 0) {
                    guardarSugerencia(objeto, reporte, resultado);
                    creadas++;
                }
            }
        }
        return new ResultadoRecalculo(candidatosObjetos.size(), candidatosReportes.size(), pares, creadas);
    }

    private void evaluarPar(ObjetoEncontrado objeto, ReportePerdida reporte) {
        if (coincidencias.existsByObjetoEncontradoIdAndReportePerdidaId(objeto.getId(), reporte.getId())) {
            return;
        }
        ResultadoMatching resultado = calcular(objeto, reporte);
        if (resultado.total().compareTo(umbral) >= 0) {
            guardarSugerencia(objeto, reporte, resultado);
        }
    }

    private void guardarSugerencia(ObjetoEncontrado objeto, ReportePerdida reporte,
                                   ResultadoMatching resultado) {
        OffsetDateTime ahora = OffsetDateTime.now();
        Coincidencia c = new Coincidencia();
        c.setObjetoEncontrado(objeto);
        c.setReportePerdida(reporte);
        c.setPuntajeCategoria(resultado.categoria());
        c.setPuntajeDescripcion(resultado.descripcion());
        c.setPuntajeUbicacion(resultado.ubicacion());
        c.setPuntajeFecha(resultado.fecha());
        c.setPuntajeCaracteristicas(resultado.caracteristicas());
        c.setPuntajeTotal(resultado.total());
        c.setEstado("SUGERIDA");
        c.setFechaCoincidencia(ahora);
        coincidencias.save(c);

        reporte.setEstado("COINCIDENCIA");
        reporte.setActualizadoEn(ahora);
        reportes.save(reporte);

        notificaciones.notificar(reporte.getUsuario(),
                "Posible coincidencia detectada",
                "Su reporte «" + reporte.getTitulo() + "» coincide con el objeto «"
                        + objeto.getTitulo() + "» con un puntaje de " + resultado.total() + "%. "
                        + "Revise el desglose y, si corresponde, inicie una reclamación.",
                "MATCH");

        auditoria.registrar(sesion.usuario().orElse(null), "COINCIDENCIA_DETECTADA", "coincidencia",
                c.getId(), Map.of("total", resultado.total().toPlainString(),
                        "objetoId", objeto.getId(), "reporteId", reporte.getId()));
    }

    private boolean mismaInstitucion(model.seguridad.Institucion a, model.seguridad.Institucion b) {
        return a != null && b != null && a.getId().equals(b.getId());
    }
}
