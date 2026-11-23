package com.backit.desktop.repositories;

import model.operativo.Coincidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoincidenciaRepository extends JpaRepository<Coincidencia, Long> {

    boolean existsByObjetoEncontradoIdAndReportePerdidaId(Long objetoId, Long reporteId);

    List<Coincidencia> findByObjetoEncontradoInstitucionIdOrderByFechaCoincidenciaDesc(Long institucionId);

    List<Coincidencia> findByReportePerdidaUsuarioIdOrderByFechaCoincidenciaDesc(Long usuarioId);

    List<Coincidencia> findAllByOrderByFechaCoincidenciaDesc();
}
