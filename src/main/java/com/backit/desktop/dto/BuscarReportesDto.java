package com.backit.desktop.dto;

import java.time.LocalDate;

/** Sprint 4 - filtros de busqueda por atributos del catalogo publico. */
public record BuscarReportesDto(
        String texto,
        Long categoriaId,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String estado) {
}
