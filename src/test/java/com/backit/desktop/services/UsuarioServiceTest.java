package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.CrearUsuarioDto;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.InstitucionRepository;
import com.backit.desktop.repositories.RolRepository;
import com.backit.desktop.repositories.UsuarioRepository;
import com.backit.desktop.security.*;
import model.seguridad.Institucion;
import model.seguridad.Rol;
import model.seguridad.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Pruebas unitarias del aprovisionamiento de cuentas (Modelo en V: diseño detallado ↔ pruebas unitarias). */
class UsuarioServiceTest {

    private static final String PASS = "Segura12345";

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final RolRepository roles = mock(RolRepository.class);
    private final InstitucionRepository instituciones = mock(InstitucionRepository.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final SesionActual sesion = new SesionActual();

    private UsuarioService service;
    private Institucion sedeA;
    private Institucion sedeB;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(usuarios, roles, instituciones, encoder, sesion,
                new AutorizacionService(sesion), auditoria);
        sedeA = institucion(1L, "Sede A");
        sedeB = institucion(2L, "Sede B");
        when(instituciones.findById(1L)).thenReturn(Optional.of(sedeA));
        when(instituciones.findById(2L)).thenReturn(Optional.of(sedeB));
        for (RolNombre r : RolNombre.values()) {
            when(roles.findByNombre(r.name())).thenReturn(Optional.of(rol(r)));
        }
        when(usuarios.existsByCorreoIgnoreCase(anyString())).thenReturn(false);
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });
    }

    @Test
    void superadminCreaAdminSedeConInstitucion() {
        iniciarSesion(RolNombre.SUPERADMIN, null);

        Usuario u = service.crear(dto(RolNombre.ADMIN_SEDE, 1L));

        assertEquals("ADMIN_SEDE", u.getRol().getNombre());
        assertEquals(sedeA, u.getInstitucion());
        assertEquals("ACTIVO", u.getEstado());
        assertNotEquals(PASS, u.getPasswordHash());
        assertTrue(encoder.matches(PASS, u.getPasswordHash()));
        verify(auditoria).registrar(any(), eq("USUARIO_CREAR"), eq("usuario"), eq(99L), any());
    }

    @Test
    void superadminCreaOtroSuperadminGlobalYRechazaInstitucion() {
        iniciarSesion(RolNombre.SUPERADMIN, null);

        Usuario u = service.crear(dto(RolNombre.SUPERADMIN, null));
        assertNull(u.getInstitucion());

        assertThrows(ValidacionException.class, () -> service.crear(dto(RolNombre.AUDITOR, 1L)));
    }

    @Test
    void superadminDebeIndicarInstitucionParaRolesLocales() {
        iniciarSesion(RolNombre.SUPERADMIN, null);
        assertThrows(ValidacionException.class, () -> service.crear(dto(RolNombre.ADMIN_SEDE, null)));
    }

    @Test
    void adminSedeCreaCustodiaYUsuarioFinalEnSuPropiaInstitucion() {
        iniciarSesion(RolNombre.ADMIN_SEDE, sedeA);

        Usuario custodia = service.crear(dto(RolNombre.PERSONAL_CUSTODIA, null));
        assertEquals(sedeA, custodia.getInstitucion());

        Usuario final_ = service.crear(dto(RolNombre.USUARIO_FINAL, 1L));
        assertEquals(sedeA, final_.getInstitucion());
    }

    @Test
    void adminSedeNoPuedeEscalarPrivilegios() {
        iniciarSesion(RolNombre.ADMIN_SEDE, sedeA);
        for (RolNombre prohibido : new RolNombre[]{RolNombre.ADMIN_SEDE, RolNombre.SUPERADMIN, RolNombre.AUDITOR}) {
            assertThrows(AccesoDenegadoException.class, () -> service.crear(dto(prohibido, 1L)), prohibido.name());
        }
        verify(usuarios, never()).save(any());
    }

    @Test
    void adminSedeNoPuedeCrearEnOtraInstitucion() {
        iniciarSesion(RolNombre.ADMIN_SEDE, sedeA);
        assertThrows(AccesoDenegadoException.class, () -> service.crear(dto(RolNombre.PERSONAL_CUSTODIA, 2L)));
    }

    @Test
    void custodiaYUsuarioFinalNoPuedenCrearCuentas() {
        iniciarSesion(RolNombre.PERSONAL_CUSTODIA, sedeA);
        assertThrows(AccesoDenegadoException.class, () -> service.crear(dto(RolNombre.USUARIO_FINAL, 1L)));
        iniciarSesion(RolNombre.USUARIO_FINAL, sedeA);
        assertThrows(AccesoDenegadoException.class, () -> service.crear(dto(RolNombre.USUARIO_FINAL, 1L)));
    }

    @Test
    void sinSesionNoSePuedeCrear() {
        assertThrows(RuntimeException.class, () -> service.crear(dto(RolNombre.USUARIO_FINAL, 1L)));
    }

    @Test
    void rechazaCorreoDuplicadoYContrasenaDebil() {
        iniciarSesion(RolNombre.SUPERADMIN, null);
        when(usuarios.existsByCorreoIgnoreCase("nuevo@uni.edu")).thenReturn(true);
        assertThrows(ValidacionException.class, () -> service.crear(dto(RolNombre.USUARIO_FINAL, 1L)));

        assertThrows(ValidacionException.class, () -> service.crear(new CrearUsuarioDto(
                "Ana", "Lopez", "otro@uni.edu", "corta", RolNombre.USUARIO_FINAL, 1L)));
    }

    @Test
    void noSePuedeCambiarElPropioEstado() {
        iniciarSesion(RolNombre.SUPERADMIN, null);
        Usuario yo = new Usuario();
        yo.setId(1L); // mismo id que el actor de la sesión
        when(usuarios.findById(1L)).thenReturn(Optional.of(yo));
        assertThrows(ValidacionException.class, () -> service.cambiarEstado(1L, EstadoUsuario.BLOQUEADO));
    }

    @Test
    void adminSedeNoGestionaCuentasDeOtraInstitucion() {
        iniciarSesion(RolNombre.ADMIN_SEDE, sedeA);
        Usuario ajeno = new Usuario();
        ajeno.setId(50L);
        ajeno.setRol(rol(RolNombre.USUARIO_FINAL));
        ajeno.setInstitucion(sedeB);
        when(usuarios.findById(50L)).thenReturn(Optional.of(ajeno));
        assertThrows(AccesoDenegadoException.class, () -> service.cambiarEstado(50L, EstadoUsuario.BLOQUEADO));
    }

    @Test
    void adminSedeBloqueaUsuarioDeSuInstitucion() {
        iniciarSesion(RolNombre.ADMIN_SEDE, sedeA);
        Usuario propio = new Usuario();
        propio.setId(51L);
        propio.setRol(rol(RolNombre.USUARIO_FINAL));
        propio.setInstitucion(sedeA);
        propio.setEstado("ACTIVO");
        when(usuarios.findById(51L)).thenReturn(Optional.of(propio));

        Usuario r = service.cambiarEstado(51L, EstadoUsuario.BLOQUEADO);

        assertEquals("BLOQUEADO", r.getEstado());
        verify(auditoria).registrar(any(), eq("USUARIO_CAMBIAR_ESTADO"), eq("usuario"), eq(51L), any());
    }

    // ---- helpers ----

    private CrearUsuarioDto dto(RolNombre rol, Long institucionId) {
        return new CrearUsuarioDto("Ana", "Lopez", "nuevo@uni.edu", PASS, rol, institucionId);
    }

    private void iniciarSesion(RolNombre rol, Institucion inst) {
        Usuario actor = new Usuario();
        actor.setId(1L);
        actor.setRol(rol(rol));
        actor.setInstitucion(inst);
        sesion.iniciar(actor, RbacCatalogo.permisosDe(rol).stream()
                .map(Enum::name).collect(Collectors.toSet()));
    }

    private static Rol rol(RolNombre nombre) {
        Rol r = new Rol();
        r.setId((long) nombre.ordinal() + 1);
        r.setNombre(nombre.name());
        return r;
    }

    private static Institucion institucion(Long id, String nombre) {
        Institucion i = new Institucion();
        i.setId(id);
        i.setNombre(nombre);
        i.setActivo(true);
        return i;
    }
}
