package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.BuscarReportesDto;
import com.backit.desktop.dto.CrearReporteDto;
import com.backit.desktop.matching.MotorCoincidenciasService;
import com.backit.desktop.repositories.CategoriaObjetoRepository;
import com.backit.desktop.repositories.InstitucionRepository;
import com.backit.desktop.repositories.ReportePerdidaRepository;
import com.backit.desktop.repositories.UbicacionRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.SesionActual;
import model.operativo.CategoriaObjeto;
import model.operativo.ReportePerdida;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportePerdidaServiceTest {

    private final ReportePerdidaRepository reportes = mock(ReportePerdidaRepository.class);
    private final CategoriaObjetoRepository categorias = mock(CategoriaObjetoRepository.class);
    private final UbicacionRepository ubicaciones = mock(UbicacionRepository.class);
    private final InstitucionRepository instituciones = mock(InstitucionRepository.class);
    private final MotorCoincidenciasService motor = mock(MotorCoincidenciasService.class);
    private final AutorizacionService autorizacion = mock(AutorizacionService.class);
    private final SesionActual sesion = mock(SesionActual.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);

    private ReportePerdidaService service;
    private Institucion institucion;
    private Usuario actor;
    private CategoriaObjeto categoria;

    @BeforeEach
    void setUp() {
        service = new ReportePerdidaService(reportes, categorias, ubicaciones, instituciones,
                motor, autorizacion, sesion, auditoria);
        institucion = new Institucion();
        institucion.setId(3L);
        actor = new Usuario();
        actor.setId(10L);
        actor.setInstitucion(institucion);
        when(sesion.requerirUsuario()).thenReturn(actor);
        categoria = new CategoriaObjeto();
        categoria.setId(1L);
        categoria.setNombre("Documentos");
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
    }

    private CrearReporteDto dto() {
        return new CrearReporteDto("DNI perdido", "Documento en carpeta azul", "Numero 12345678Z",
                1L, null, null, LocalDate.now());
    }

    @Test
    void crearAsignaPropietarioEstadoActivoYEjecutaMatching() {
        ReportePerdida r = service.crear(dto());

        assertEquals("ACTIVO", r.getEstado());
        assertEquals(actor, r.getUsuario());
        assertEquals(institucion, r.getInstitucion());
        verify(reportes).save(any(ReportePerdida.class));
        verify(motor).evaluarNuevoReporte(r);
    }

    @Test
    void buscarFiltraPorTextoYCategoria() {
        ReportePerdida r = reporte("DNI perdido", 1L);
        ReportePerdida otro = reporte("Laptop robada", 2L);
        when(reportes.findByInstitucionIdOrderByCreadoEnDesc(3L)).thenReturn(List.of(r, otro));

        List<ReportePerdida> resultado = service.buscar(new BuscarReportesDto("dni", null, null, null, null));
        assertEquals(1, resultado.size());
        assertEquals(r.getId(), resultado.get(0).getId());

        assertEquals(1, service.buscar(new BuscarReportesDto(null, 1L, null, null, null)).size());
    }

    @Test
    void buscarRespetaRangoDeFechas() {
        ReportePerdida r = reporte("DNI", 1L);
        r.setFechaPerdida(OffsetDateTime.parse("2026-05-10T00:00:00Z"));
        when(reportes.findByInstitucionIdOrderByCreadoEnDesc(3L)).thenReturn(List.of(r));

        assertTrue(service.buscar(new BuscarReportesDto(null, null,
                LocalDate.of(2026, 6, 1), null, null)).isEmpty());
        assertEquals(1, service.buscar(new BuscarReportesDto(null, null,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), null)).size());
    }

    private ReportePerdida reporte(String titulo, Long categoriaId) {
        ReportePerdida r = new ReportePerdida();
        r.setId(categoriaId);
        r.setTitulo(titulo);
        r.setDescripcion("Descripcion publica");
        r.setInstitucion(institucion);
        CategoriaObjeto c = new CategoriaObjeto();
        c.setId(categoriaId);
        r.setCategoria(c);
        return r;
    }
}
