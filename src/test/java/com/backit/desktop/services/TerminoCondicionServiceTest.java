package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.CrearTerminoDto;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.AceptacionTerminosRepository;
import com.backit.desktop.repositories.TerminoCondicionRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.SesionActual;
import model.sistema.AceptacionTerminos;
import model.sistema.TerminoCondicion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TerminoCondicionServiceTest {

    private final TerminoCondicionRepository terminos = mock(TerminoCondicionRepository.class);
    private final AceptacionTerminosRepository aceptaciones = mock(AceptacionTerminosRepository.class);
    private final AutorizacionService autorizacion = mock(AutorizacionService.class);
    private final SesionActual sesion = mock(SesionActual.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);

    private TerminoCondicionService service;
    private TerminoCondicion vigente;

    @BeforeEach
    void setUp() {
        service = new TerminoCondicionService(terminos, aceptaciones, autorizacion, sesion, auditoria);
        vigente = new TerminoCondicion();
        vigente.setId(1L);
        vigente.setVersion("1.0");
        vigente.setTitulo("Terminos");
        vigente.setContenido("Contenido de prueba");
        vigente.setContenidoHash("abc");
        vigente.setFechaVigencia(OffsetDateTime.now());
        vigente.setActivo(true);
        when(terminos.findByActivoTrueOrderByFechaVigenciaDesc()).thenReturn(List.of(vigente));
    }

    @Test
    void requiereAceptacionCuandoNoHayEvidencia() {
        when(aceptaciones.existsByUsuarioIdAndTerminoCondicionIdAndAceptadoTrueAndRevocadoEnIsNull(10L, 1L))
                .thenReturn(false);
        assertTrue(service.requiereAceptacion(usuario(10L)));
    }

    @Test
    void noRequiereAceptacionCuandoYaAcepto() {
        when(aceptaciones.existsByUsuarioIdAndTerminoCondicionIdAndAceptadoTrueAndRevocadoEnIsNull(10L, 1L))
                .thenReturn(true);
        assertFalse(service.requiereAceptacion(usuario(10L)));
    }

    @Test
    void registrarDecisionGuardaEvidenciaConHashYRevocaLaAnterior() {
        AceptacionTerminos anterior = new AceptacionTerminos();
        anterior.setId(5L);
        anterior.setRevocadoEn(null);
        when(aceptaciones.findTopByUsuarioIdAndTerminoCondicionIdOrderByFechaAceptacionDesc(10L, 1L))
                .thenReturn(Optional.of(anterior));

        service.registrarDecision(usuario(10L), true);

        assertNotNull(anterior.getRevocadoEn());
        verify(aceptaciones, atLeastOnce()).save(any(AceptacionTerminos.class));
        verify(auditoria).registrar(any(), eq("TERMINOS_ACEPTAR"), eq("termino_condicion"), eq(1L), any());
    }

    @Test
    void publicarNuevaVersionDesactivaLaAnterior() {
        when(sesion.requerirUsuario()).thenReturn(usuario(10L));
        when(terminos.findByVersion("2.0")).thenReturn(Optional.empty());

        service.publicar(new CrearTerminoDto("2.0", "Nuevos terminos", "Contenido nuevo largo y claro"));

        assertFalse(vigente.getActivo());
        verify(autorizacion).requerir(com.backit.desktop.security.PermisoCodigo.TERMINOS_GESTIONAR);
        verify(terminos, times(2)).save(any(TerminoCondicion.class));
    }

    @Test
    void publicarRechazaVersionDuplicada() {
        when(terminos.findByVersion("1.0")).thenReturn(Optional.of(vigente));
        assertThrows(ValidacionException.class,
                () -> service.publicar(new CrearTerminoDto("1.0", "Terminos", "Contenido")));
    }

    private model.seguridad.Usuario usuario(Long id) {
        model.seguridad.Usuario u = new model.seguridad.Usuario();
        u.setId(id);
        u.setCorreo("u@test.com");
        return u;
    }
}
