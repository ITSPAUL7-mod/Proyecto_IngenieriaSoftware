package com.backit.desktop.repositories;

import model.operativo.ReportePerdida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportePerdidaRepository extends JpaRepository<ReportePerdida, Long> {

    List<ReportePerdida> findByInstitucionIdOrderByCreadoEnDesc(Long institucionId);

    List<ReportePerdida> findAllByOrderByCreadoEnDesc();

    List<ReportePerdida> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);

    /** Candidatos del motor de coincidencias. */
    List<ReportePerdida> findByEstadoIn(List<String> estados);
}
