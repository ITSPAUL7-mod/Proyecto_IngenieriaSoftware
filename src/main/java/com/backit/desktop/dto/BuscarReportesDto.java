package com.backit.desktop.dto;

import java.time.LocalDate;

//filtros de busqueda por atributos del catalogo.
public record BuscarReportesDto(
        String texto,
        Long categoriaId,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String estado) {
}
