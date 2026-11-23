package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.RegistrarObjetoDto;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.matching.MotorCoincidenciasService;
import com.backit.desktop.qr.QrService;
import com.backit.desktop.repositories.CategoriaObjetoRepository;
import com.backit.desktop.repositories.InstitucionRepository;
import com.backit.desktop.repositories.ObjetoEncontradoRepository;
import com.backit.desktop.repositories.UbicacionRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.storage.AlmacenamientoService;
import model.operativo.CategoriaObjeto;
import model.operativo.ObjetoEncontrado;
import model.operativo.Ubicacion;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ObjetoEncontradoServiceTest {

    private final ObjetoEncontradoRepository objetos = mock(ObjetoEncontradoRepository.class);
    private final CategoriaObjetoRepository categorias = mock(CategoriaObjetoRepository.class);
    private final UbicacionRepository ubicaciones = mock(UbicacionRepository.class);
    private final InstitucionRepository instituciones = mock(InstitucionRepository.class);
    private final AlmacenamientoService almacenamiento = mock(AlmacenamientoService.class);
    private final QrService qr = mock(QrService.class);
    private final MotorCoincidenciasService motor = mock(MotorCoincidenciasService.class);
    private final AutorizacionService autorizacion = mock(AutorizacionService.class);
    private final SesionActual sesion = mock(SesionActual.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);

    private ObjetoEncontradoService service;
    private Institucion institucion;
    private Usuario actor;

    @BeforeEach
    void setUp() {
        service = new ObjetoEncontradoService(objetos, categorias, ubicaciones, instituciones,
                almacenamiento, qr, motor, autorizacion, sesion, auditoria);
        institucion = new Institucion();
        institucion.setId(3L);
        actor = new Usuario();
        actor.setId(10L);
        actor.setInstitucion(institucion);
        when(sesion.requerirUsuario()).thenReturn(actor);

        CategoriaObjeto categoria = new CategoriaObjeto();
        categoria.setId(1L);
        categoria.setNombre("Electrónica");
        categoria.setActivo(true);
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(2L);
        ubicacion.setInstitucion(institucion);
        when(ubicaciones.findById(2L)).thenReturn(Optional.of(ubicacion));
    }

    private RegistrarObjetoDto dto() {
        return new RegistrarObjetoDto("Laptop negra", "Laptop con cargador", "Serie ABC123",
                1L, 2L, null, LocalDate.now());
    }

    @Test
    void registrarGeneraQrEstadoInicialYAudiya() {
        ObjetoEncontrado objeto = service.registrar(dto(), null, null);

        assertEquals("REGISTRADO", objeto.getEstado());
        assertNotNull(objeto.getCodigoQr());
        assertEquals(actor, objeto.getEncontradoPor());
        verify(objetos).save(any(ObjetoEncontrado.class));
        verify(motor).evaluarNuevoObjeto(objeto);
        verify(auditoria).registrar(any(), eq("OBJETO_REGISTRAR"), eq("objeto_encontrado"), any(), any());
    }

    @Test
    void registrarGuardaFotoConHash() throws Exception {
        when(almacenamiento.guardarFoto(any(), any(), any()))
                .thenReturn(new AlmacenamientoService.ArchivoGuardado("/privada/foto.png", "hash-64"));

        ObjetoEncontrado objeto = service.registrar(dto(), new byte[]{1, 2, 3}, "foto.png");

        assertEquals("/privada/foto.png", objeto.getFotoRuta());
        assertEquals("hash-64", objeto.getFotoHash());
    }

    @Test
    void rechazaUbicacionDeOtraInstitucion() {
        Institucion otra = new Institucion();
        otra.setId(99L);
        Ubicacion ajena = new Ubicacion();
        ajena.setId(8L);
        ajena.setInstitucion(otra);
        when(ubicaciones.findById(2L)).thenReturn(Optional.of(ajena));

        assertThrows(AccesoDenegadoException.class, () -> service.registrar(dto(), null, null));
    }
}
