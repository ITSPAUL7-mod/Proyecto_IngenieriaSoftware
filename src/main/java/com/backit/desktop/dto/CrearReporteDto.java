package com.backit.desktop.dto;

import java.time.LocalDate;

/** Sprint 4 - alta de un reporte de pérdida. */
public record CrearReporteDto(
        String titulo,
        String descripcion,
        String caracteristicasPrivadas,
        Long categoriaId,
        Long ubicacionId,
        Long institucionId,
        LocalDate fechaPerdida) {
}
