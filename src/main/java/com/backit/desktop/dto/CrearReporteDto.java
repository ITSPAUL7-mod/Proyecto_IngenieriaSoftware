package com.backit.desktop.dto;

import java.time.LocalDate;

// creacion de un reporte de pérdida del objeto.
public record CrearReporteDto(
        String titulo,
        String descripcion,
        String caracteristicasPrivadas,
        Long categoriaId,
        Long ubicacionId,
        Long institucionId,
        LocalDate fechaPerdida) {
}
