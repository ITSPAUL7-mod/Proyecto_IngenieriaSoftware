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
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.storage.AlmacenamientoService;
import model.operativo.Coincidencia;
import model.operativo.ObjetoEncontrado;
import model.operativo.ReportePerdida;
import model.reclamacion.Reclamacion;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReclamacionServiceTest {

    private final ReclamacionRepository reclamaciones = mock(ReclamacionRepository.class);
    private final EvidenciaDevolucionRepository evidencias = mock(EvidenciaDevolucionRepository.class);
    private final CoincidenciaRepository coincidencias = mock(CoincidenciaRepository.class);
    private final ObjetoEncontradoRepository objetos = mock(ObjetoEncontradoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final AlmacenamientoService almacenamiento = mock(AlmacenamientoService.class);
    private final NotificacionService notificaciones = mock(NotificacionService.class);
    private final AutorizacionService autorizacion = mock(AutorizacionService.class);
    private final SesionActual sesion = mock(SesionActual.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);

    private ReclamacionService service;
    private Usuario actor;
    private Coincidencia coincidencia;
    private ObjetoEncontrado objeto;

    @BeforeEach
    void setUp() {
        service = new ReclamacionService(reclamaciones, evidencias, coincidencias, objetos, usuarios,
                almacenamiento, notificaciones, autorizacion, sesion, auditoria);

        Institucion sede = new Institucion();
        sede.setId(3L);
        actor = new Usuario();
        actor.setId(10L);
        actor.setCorreo("dueno@uni.edu");
        actor.setInstitucion(sede);
        when(sesion.requerirUsuario()).thenReturn(actor);

        ReportePerdida reporte = new ReportePerdida();
        reporte.setId(200L);
        reporte.setUsuario(actor);

        objeto = new ObjetoEncontrado();
        objeto.setId(100L);
        objeto.setTitulo("Laptop negra");

        coincidencia = new Coincidencia();
        coincidencia.setId(1L);
        coincidencia.setReportePerdida(reporte);
        coincidencia.setObjetoEncontrado(objeto);
        coincidencia.setEstado("SUGERIDA");
        when(coincidencias.findById(1L)).thenReturn(Optional.of(coincidencia));
        when(reclamaciones.findByCoincidenciaId(1L)).thenReturn(Optional.empty());
        when(usuarios.findByRolNombreAndInstitucionIdOrderByApellidoAscNombreAsc(anyString(), any()))
                .thenReturn(List.of());
        when(usuarios.findByRolNombreOrderByApellidoAscNombreAsc(anyString())).thenReturn(List.of());
    }

    private CrearReclamacionDto dto() {
        return new CrearReclamacionDto(1L);
    }

    @Test
    void crearDejaPendienteMarcaCoincidenciaYAudiya() {
        Reclamacion r = service.crear(dto(), null, null, null, null);

        assertEquals("PENDIENTE", r.getEstado());
        assertNotNull(r.getDocumentoVerificadoHash());
        assertEquals("RECLAMADA", coincidencia.getEstado());
        verify(reclamaciones).save(any(Reclamacion.class));
        verify(auditoria).registrar(any(), eq("RECLAMACION_CREAR"), eq("reclamacion"), any(), any());
        verify(notificaciones).notificar(eq(actor), contains("Reclamación registrada"), anyString(), eq("RECLAMO"));
    }

    @Test
    void crearRechazaAQuienNoEsDuenoDelReporte() {
        Usuario otro = new Usuario();
        otro.setId(99L);
        when(sesion.requerirUsuario()).thenReturn(otro);

        assertThrows(AccesoDenegadoException.class, () -> service.crear(dto(), null, null, null, null));
    }

    @Test
    void crearRechazaCoincidenciaYaReclamada() {
        when(reclamaciones.findByCoincidenciaId(1L)).thenReturn(Optional.of(new Reclamacion()));
        assertThrows(ValidacionException.class, () -> service.crear(dto(), null, null, null, null));
    }

    @Test
    void aprobarCambiaEstadoDelObjetoAReclamado() {
        Reclamacion r = new Reclamacion();
        r.setId(5L);
        r.setEstado("PENDIENTE");
        r.setCoincidencia(coincidencia);
        r.setUsuarioSolicitante(actor);
        when(reclamaciones.findById(5L)).thenReturn(Optional.of(r));

        service.aprobar(5L);

        assertEquals("APROBADA", r.getEstado());
        assertEquals("RECLAMADO", objeto.getEstado());
        verify(objetos).save(objeto);
    }

    @Test
    void rechazarExigeMotivo() {
        Reclamacion r = new Reclamacion();
        r.setId(5L);
        r.setEstado("PENDIENTE");
        r.setCoincidencia(coincidencia);
        r.setUsuarioSolicitante(actor);
        when(reclamaciones.findById(5L)).thenReturn(Optional.of(r));

        assertThrows(ValidacionException.class, () -> service.rechazar(5L, "  "));
        service.rechazar(5L, "La evidencia no corresponde al objeto");
        assertEquals("RECHAZADA", r.getEstado());
        assertNotNull(r.getMotivoRechazo());
    }
}
