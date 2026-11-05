package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.exceptions.AutenticacionException;
import com.backit.desktop.repositories.RolPermisoRepository;
import com.backit.desktop.repositories.UsuarioRepository;
import com.backit.desktop.security.SesionActual;
import model.seguridad.Rol;
import model.seguridad.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final RolPermisoRepository rolPermisos = mock(RolPermisoRepository.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final SesionActual sesion = new SesionActual();
    private AuthService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new AuthService(usuarios, rolPermisos, encoder, sesion, auditoria);
        Rol rol = new Rol();
        rol.setId(2L);
        rol.setNombre("ADMIN_SEDE");
        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setRol(rol);
        usuario.setCorreo("admin@uni.edu");
        usuario.setPasswordHash(encoder.encode("Segura12345"));
        usuario.setEstado("ACTIVO");
        when(usuarios.findByCorreoIgnoreCase("admin@uni.edu")).thenReturn(Optional.of(usuario));
        when(rolPermisos.findCodigosActivosByRolId(2L)).thenReturn(List.of("USUARIO_VER"));
    }

    @Test
    void loginCorrectoAbreSesionConPermisos() {
        service.autenticar(" Admin@Uni.edu ", "Segura12345");
        assertTrue(sesion.usuario().isPresent());
        assertEquals("ADMIN_SEDE", sesion.rolActual().name());
    }

    @Test
    void mensajeIgualParaCorreoInexistenteYContrasenaMala() {
        AutenticacionException a = assertThrows(AutenticacionException.class,
                () -> service.autenticar("nadie@uni.edu", "x"));
        AutenticacionException b = assertThrows(AutenticacionException.class,
                () -> service.autenticar("admin@uni.edu", "mala"));
        assertEquals(a.getMessage(), b.getMessage());
        assertTrue(sesion.usuario().isEmpty());
    }

    @Test
    void cuentaBloqueadaAdministrativamenteNoEntra() {
        usuario.setEstado("BLOQUEADO");
        assertThrows(AutenticacionException.class, () -> service.autenticar("admin@uni.edu", "Segura12345"));
        assertTrue(sesion.usuario().isEmpty());
    }

    @Test
    void tras5FallosSePausaAunqueLaContrasenaSeaCorrecta() {
        for (int i = 0; i < AuthService.MAX_INTENTOS; i++) {
            assertThrows(AutenticacionException.class, () -> service.autenticar("admin@uni.edu", "mala"));
        }
        AutenticacionException e = assertThrows(AutenticacionException.class,
                () -> service.autenticar("admin@uni.edu", "Segura12345"));
        assertTrue(e.getMessage().contains("intentos"));
        assertTrue(sesion.usuario().isEmpty());
    }
}
