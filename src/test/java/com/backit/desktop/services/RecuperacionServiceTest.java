package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.UsuarioRepository;
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

class RecuperacionServiceTest {

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final AuthService authService = mock(AuthService.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    private RecuperacionService service;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new RecuperacionService(usuarios, authService, encoder, auditoria);
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("USUARIO_FINAL");
        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setRol(rol);
        usuario.setCorreo("user@uni.edu");
        usuario.setPasswordHash(encoder.encode("ClaveInicial1"));
        when(usuarios.findByCorreoIgnoreCase("user@uni.edu")).thenReturn(Optional.of(usuario));
        when(usuarios.findById(10L)).thenReturn(Optional.of(usuario));
    }

    @Test
    void tokenValidoPermiteCambiarClaveYDesbloquea() {
        String token = service.solicitar(" user@uni.edu ");
        assertNotNull(token);

        service.restablecer(token, "NuevaClave123");

        assertTrue(encoder.matches("NuevaClave123", usuario.getPasswordHash()));
        verify(authService).desbloquear("user@uni.edu");
        verify(auditoria).registrar(any(), eq("RECUPERACION_COMPLETADA"), eq("usuario"), eq(10L), any());
    }

    @Test
    void tokenInvalidoEsRechazado() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> service.restablecer("token-inexistente", "NuevaClave123"));
        assertTrue(e.getMessage().contains("código"));
        verify(usuarios, never()).save(any());
    }

    @Test
    void tokenSoloSePuedeUsarUnaVez() {
        String token = service.solicitar("user@uni.edu");
        service.restablecer(token, "NuevaClave123");
        assertThrows(ValidacionException.class, () -> service.restablecer(token, "OtraClave123"));
    }

    @Test
    void correoInexistenteNoRevelaInformacion() {
        when(usuarios.findByCorreoIgnoreCase("nadie@uni.edu")).thenReturn(Optional.empty());
        String token = service.solicitar("nadie@uni.edu");
        assertNotNull(token);
        verify(auditoria, never()).registrar(any(), anyString(), any(), any(), any());
    }
}
