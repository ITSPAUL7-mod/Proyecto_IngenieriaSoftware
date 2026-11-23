package com.backit.desktop.repositories;

import model.sistema.AceptacionTerminos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AceptacionTerminosRepository extends JpaRepository<AceptacionTerminos, Long> {

    /** True cuando existe una aceptacion vigente (no revocada) para ese usuario y version. */
    boolean existsByUsuarioIdAndTerminoCondicionIdAndAceptadoTrueAndRevocadoEnIsNull(
            Long usuarioId, Long terminoId);

    /** Ultima decision del usuario sobre una version, para poder revocarla antes de registrar otra. */
    java.util.Optional<AceptacionTerminos>
    findTopByUsuarioIdAndTerminoCondicionIdOrderByFechaAceptacionDesc(Long usuarioId, Long terminoId);
}
