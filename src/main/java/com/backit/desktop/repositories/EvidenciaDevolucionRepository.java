package com.backit.desktop.repositories;

import model.reclamacion.EvidenciaDevolucion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenciaDevolucionRepository extends JpaRepository<EvidenciaDevolucion, Long> {

    List<EvidenciaDevolucion> findByReclamacionIdOrderByCreadoEnDesc(Long reclamacionId);
}
