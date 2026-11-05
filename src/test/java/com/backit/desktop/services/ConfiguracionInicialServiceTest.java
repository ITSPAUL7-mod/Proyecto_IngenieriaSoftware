package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.dto.ConfiguracionInicialDto;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.RolRepository;
import com.backit.desktop.repositories.UsuarioRepository;
import model.seguridad.Institucion;
import model.seguridad.Rol;
import model.seguridad.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConfiguracionInicialServiceTest {

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final RolRepository roles = mock(RolRepository.class);
    private final InstitucionService instituciones = mock(InstitucionService.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private ConfiguracionInicialService service;

    @BeforeEach
    void setUp() {
        service = new ConfiguracionInicialService(usuarios, roles, instituciones, encoder, auditoria);
        Rol superadmin = new Rol();
        superadmin.setId(1L);
        superadmin.setNombre("SUPERADMIN");
        when(roles.findByNombre("SUPERADMIN")).thenReturn(Optional.of(superadmin));
        Institucion inst = new Institucion();
        inst.setId(7L);
        inst.setNombre("Universidad");
        when(instituciones.crearSinSesion(any())).thenReturn(inst);
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
    }

    @Test
    void creaElPrimerSuperadminGlobalConHashBcrypt() {
        when(usuarios.existsByRolNombre("SUPERADMIN")).thenReturn(false);

        Usuario u = service.crearSuperAdminInicial(dto("Segura12345", "Segura12345"));

        assertNull(u.getInstitucion());
        assertEquals("SUPERADMIN", u.getRol().getNombre());
        assertTrue(encoder.matches("Segura12345", u.getPasswordHash()));
        verify(auditoria).registrar(any(), eq("SUPERADMIN_INICIAL_CREADO"), eq("usuario"), eq(1L), any());
    }

    @Test
    void sePuedeUsarUnaSolaVez() {
        when(usuarios.existsByRolNombre("SUPERADMIN")).thenReturn(true);
        assertFalse(service.requiereConfiguracionInicial());
        assertThrows(AccesoDenegadoException.class,
                () -> service.crearSuperAdminInicial(dto("Segura12345", "Segura12345")));
        verify(usuarios, never()).save(any());
    }

    @Test
    void exigeConfirmacionYContrasenaFuerte() {
        when(usuarios.existsByRolNombre("SUPERADMIN")).thenReturn(false);
        assertThrows(ValidacionException.class, () -> service.crearSuperAdminInicial(dto("Segura12345", "Otra12345")));
        assertThrows(ValidacionException.class, () -> service.crearSuperAdminInicial(dto("debil", "debil")));
    }

    private static ConfiguracionInicialDto dto(String pass, String confirmacion) {
        return new ConfiguracionInicialDto("Universidad", "Paul", "Lumiquinga", "paul@uni.edu", pass, confirmacion);
    }
}
