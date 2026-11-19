package com.backit.desktop.repositories;

import model.operativo.CategoriaObjeto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaObjetoRepository extends JpaRepository<CategoriaObjeto, Long> {

    List<CategoriaObjeto> findByActivoTrueOrderByNombreAsc();

    Optional<CategoriaObjeto> findByNombreIgnoreCase(String nombre);
}
