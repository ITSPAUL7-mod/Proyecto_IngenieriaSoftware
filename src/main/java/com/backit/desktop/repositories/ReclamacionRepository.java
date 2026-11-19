package com.backit.desktop.repositories;

import model.reclamacion.Reclamacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReclamacionRepository extends JpaRepository<Reclamacion, Long> {

    Optional<Reclamacion> findByCoincidenciaId(Long coincidenciaId);

    List<Reclamacion> findByUsuarioSolicitanteIdOrderByFechaSolicitudDesc(Long usuarioId);

    List<Reclamacion> findByCoincidenciaObjetoEncontradoInstitucionIdOrderByFechaSolicitudDesc(Long institucionId);

    List<Reclamacion> findAllByOrderByFechaSolicitudDesc();
}
