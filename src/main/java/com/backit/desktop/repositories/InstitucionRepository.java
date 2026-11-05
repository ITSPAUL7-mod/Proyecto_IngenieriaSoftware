package com.backit.desktop.repositories;

import model.seguridad.Institucion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstitucionRepository extends JpaRepository<Institucion, Long> {
    List<Institucion> findByActivoTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByRuc(String ruc);
}
