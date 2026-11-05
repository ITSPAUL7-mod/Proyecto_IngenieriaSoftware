package com.backit.desktop.repositories;

import model.seguridad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByRolNombre(String nombreRol);

    List<Usuario> findByInstitucionIdOrderByApellidoAscNombreAsc(Long institucionId);

    List<Usuario> findAllByOrderByApellidoAscNombreAsc();
}
