package com.backit.desktop.repositories;

import model.operativo.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    List<Ubicacion> findByInstitucionIdOrderByNombreAsc(Long institucionId);

    List<Ubicacion> findAllByOrderByNombreAsc();

    boolean existsByInstitucionIdAndNombreIgnoreCase(Long institucionId, String nombre);
}
