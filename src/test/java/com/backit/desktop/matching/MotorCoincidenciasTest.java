package com.backit.desktop.matching;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.repositories.CoincidenciaRepository;
import com.backit.desktop.repositories.ObjetoEncontradoRepository;
import com.backit.desktop.repositories.ReportePerdidaRepository;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.services.NotificacionService;
import model.operativo.CategoriaObjeto;
import model.operativo.Coincidencia;
import model.operativo.ObjetoEncontrado;
import model.operativo.ReportePerdida;
import model.operativo.Ubicacion;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MotorCoincidenciasTest {

    private final ReportePerdidaRepository reportes = mock(ReportePerdidaRepository.class);
    private final ObjetoEncontradoRepository objetos = mock(ObjetoEncontradoRepository.class);
    private final CoincidenciaRepository coincidencias = mock(CoincidenciaRepository.class);
    private final NotificacionService notificaciones = mock(NotificacionService.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);
    private final SesionActual sesion = mock(SesionActual.class);

    private MotorCoincidenciasService motor;
    private ObjetoEncontrado objeto;
    private ReportePerdida reporte;
    private Institucion institucion;
    private CategoriaObjeto categoria;
    private Ubicacion ubicacion;
    private Usuario dueno;

    @BeforeEach
    void setUp() {
        motor = new MotorCoincidenciasService(
                List.of(new CriterioCategoria(), new CriterioDescripcion(), new CriterioUbicacion(),
                        new CriterioFecha(), new CriterioCaracteristicas()),
                reportes, objetos, coincidencias, notificaciones, auditoria, sesion,
                new BigDecimal("70"));

        institucion = new Institucion();
        institucion.setId(3L);
        categoria = new CategoriaObjeto();
        categoria.setId(1L);
        ubicacion = new Ubicacion();
        ubicacion.setId(2L);
        ubicacion.setInstitucion(institucion);
        dueno = new Usuario();
        dueno.setId(10L);

        objeto = new ObjetoEncontrado();
        objeto.setId(100L);
        objeto.setInstitucion(institucion);
        objeto.setCategoria(categoria);
        objeto.setUbicacion(ubicacion);
        objeto.setTitulo("Laptop negra");
        objeto.setDescripcion("laptop negra con cargador y mochila");
        objeto.setCaracteristicasPrivadas("serie ABC123 teclado español");
        objeto.setFechaEncontrado(OffsetDateTime.parse("2026-05-10T10:00:00Z"));

        reporte = new ReportePerdida();
        reporte.setId(200L);
        reporte.setInstitucion(institucion);
        reporte.setUsuario(dueno);
        reporte.setCategoria(categoria);
        reporte.setUbicacionPerdida(ubicacion);
        reporte.setTitulo("Laptop perdida");
        reporte.setDescripcion("laptop negra con cargador y mochila");
        reporte.setCaracteristicasPrivadas("serie ABC123 teclado español");
        reporte.setFechaPerdida(OffsetDateTime.parse("2026-05-10T09:00:00Z"));
        reporte.setEstado("ACTIVO");
    }

    @Test
    void coincidenciaExactaDaCienPuntos() {
        ResultadoMatching r = motor.calcular(objeto, reporte);
        assertEquals(0, new BigDecimal("100.00").compareTo(r.total()));
        assertEquals(0, new BigDecimal("100").compareTo(r.categoria()));
        assertEquals(0, new BigDecimal("100").compareTo(r.ubicacion()));
        assertEquals(0, new BigDecimal("100").compareTo(r.fecha()));
    }

    @Test
    void categoriaDistintaReduceElPuntaje() {
        CategoriaObjeto otra = new CategoriaObjeto();
        otra.setId(9L);
        reporte.setCategoria(otra);
        assertTrue(motor.calcular(objeto, reporte).total().compareTo(new BigDecimal("100")) < 0);
    }

    @Test
    void crearSugerenciaNotificaYCambiaEstadoDelReporte() {
        when(reportes.findByEstadoIn(anyList())).thenReturn(List.of(reporte));

        motor.evaluarNuevoObjeto(objeto);

        verify(coincidencias).save(argThat(c ->
                "SUGERIDA".equals(c.getEstado()) && c.getPuntajeTotal().compareTo(new BigDecimal("70")) >= 0));
        verify(notificaciones).notificar(eq(dueno), anyString(), anyString(), eq("MATCH"));
        assertEquals("COINCIDENCIA", reporte.getEstado());
    }

    @Test
    void noCreaNadaPorDebajoDelUmbral() {
        reporte.setDescripcion("billete de diez euros");
        reporte.setCaracteristicasPrivadas("sin señas");
        when(reportes.findByEstadoIn(anyList())).thenReturn(List.of(reporte));

        motor.evaluarNuevoObjeto(objeto);

        verify(coincidencias, never()).save(any(Coincidencia.class));
        verify(notificaciones, never()).notificar(any(), anyString(), anyString(), anyString());
    }

    @Test
    void noDuplicaCoincidencias() {
        when(reportes.findByEstadoIn(anyList())).thenReturn(List.of(reporte));
        when(coincidencias.existsByObjetoEncontradoIdAndReportePerdidaId(100L, 200L)).thenReturn(true);

        motor.evaluarNuevoObjeto(objeto);

        verify(coincidencias, never()).save(any(Coincidencia.class));
    }
}
