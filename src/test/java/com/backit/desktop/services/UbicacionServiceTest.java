package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.CrearUbicacionDto;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.InstitucionRepository;
import com.backit.desktop.repositories.PuntoEntregaRepository;
import com.backit.desktop.repositories.UbicacionRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.SesionActual;
import model.operativo.Ubicacion;
import model.reclamacion.PuntoEntrega;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UbicacionServiceTest {

    private final UbicacionRepository ubicaciones = mock(UbicacionRepository.class);
    private final PuntoEntregaRepository puntos = mock(PuntoEntregaRepository.class);
    private final InstitucionRepository instituciones = mock(InstitucionRepository.class);
    private final AutorizacionService autorizacion = mock(AutorizacionService.class);
    private final SesionActual sesion = mock(SesionActual.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);

    private UbicacionService service;
    private Institucion institucion;
    private Usuario actor;

    @BeforeEach
    void setUp() {
        service = new UbicacionService(ubicaciones, puntos, instituciones, autorizacion, sesion, auditoria);
        institucion = new Institucion();
        institucion.setId(3L);
        institucion.setActivo(true);
        actor = new Usuario();
        actor.setId(10L);
        actor.setInstitucion(institucion);
        when(sesion.requerirUsuario()).thenReturn(actor);
        when(instituciones.findById(3L)).thenReturn(Optional.of(institucion));
    }

    private CrearUbicacionDto dto(String nombre, String tipo, Long padreId, String horario, Long institucionId) {
        return new CrearUbicacionDto(nombre, tipo, null, null, padreId, horario, institucionId);
    }

    @Test
    void crearUbicacionGeneraPuntoDeEntregaCuandoSeIndicaHorario() {
        service.crear(dto("Biblioteca", "EDIFICIO", null, "Lun-Vie 8-17", null));

        verify(ubicaciones).save(argThat(u -> u.getInstitucion().getId().equals(3L)));
        verify(puntos).save(any(PuntoEntrega.class));
        verify(auditoria).registrar(any(), eq("UBICACION_CREAR"), eq("ubicacion"), any(), any());
    }

    @Test
    void crearSinHorarioNoGeneraPuntoDeEntrega() {
        service.crear(dto("Bodega 1", "BODEGA", null, "  ", null));
        verify(puntos, never()).save(any());
    }

    @Test
    void rechazaTipoFueraDelCatalogo() {
        assertThrows(ValidacionException.class,
                () -> service.crear(dto("X", "GARAGE", null, null, null)));
    }

    @Test
    void rechazaPadreDeOtraInstitucion() {
        Institucion otra = new Institucion();
        otra.setId(99L);
        Ubicacion padre = new Ubicacion();
        padre.setId(7L);
        padre.setInstitucion(otra);
        when(ubicaciones.findById(7L)).thenReturn(Optional.of(padre));

        assertThrows(AccesoDenegadoException.class,
                () -> service.crear(dto("Piso 2", "PISO", 7L, null, null)));
    }

    @Test
    void visiblesFiltraPorInstitucionDelActor() {
        when(ubicaciones.findByInstitucionIdOrderByNombreAsc(3L)).thenReturn(List.of());
        assertTrue(service.visibles().isEmpty());
        verify(ubicaciones).findByInstitucionIdOrderByNombreAsc(3L);
    }

    @Test
    void cuentaGlobalPuedeCrearSiIndicaInstitucion() {
        actor.setInstitucion(null); // SUPERADMIN

        service.crear(dto("Sede Norte", "SEDE", null, null, 3L));

        verify(ubicaciones).save(argThat(u -> u.getInstitucion().getId().equals(3L)));
    }

    @Test
    void cuentaGlobalDebeIndicarInstitucion() {
        actor.setInstitucion(null);
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> service.crear(dto("Sede Norte", "SEDE", null, null, null)));
        assertTrue(e.getMessage().contains("institución"));
    }

    @Test
    void cuentaLocalNoPuedeCrearEnOtraInstitucion() {
        AccesoDenegadoException e = assertThrows(AccesoDenegadoException.class,
                () -> service.crear(dto("Sede Ajena", "SEDE", null, null, 99L)));
        assertTrue(e.getMessage().contains("otra institución"));
    }
}
