package com.backit.desktop.repositories;

import model.operativo.ObjetoEncontrado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ObjetoEncontradoRepository extends JpaRepository<ObjetoEncontrado, Long> {

    List<ObjetoEncontrado> findByInstitucionIdOrderByCreadoEnDesc(Long institucionId);

    List<ObjetoEncontrado> findAllByOrderByCreadoEnDesc();

    List<ObjetoEncontrado> findByEstadoInAndInstitucionId(List<String> estados, Long institucionId);

    List<ObjetoEncontrado> findByEstadoIn(List<String> estados);

    Optional<ObjetoEncontrado> findByCodigoQr(UUID codigoQr);
}
