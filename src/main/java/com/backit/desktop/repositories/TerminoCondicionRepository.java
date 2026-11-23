package com.backit.desktop.repositories;

import model.sistema.TerminoCondicion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TerminoCondicionRepository extends JpaRepository<TerminoCondicion, Long> {

    Optional<TerminoCondicion> findByVersion(String version);

    /** Version vigente: la activa mas reciente. */
    List<TerminoCondicion> findByActivoTrueOrderByFechaVigenciaDesc();
}
