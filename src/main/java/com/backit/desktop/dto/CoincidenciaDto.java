package com.backit.desktop.dto;

import java.math.BigDecimal;

/** Vista resumida y explicable de una coincidencia (Sprint 5). */
public record CoincidenciaDto(
        Long id,
        String objetoTitulo,
        String reporteTitulo,
        BigDecimal total,
        String estado,
        BigDecimal categoria,
        BigDecimal descripcion,
        BigDecimal ubicacion,
        BigDecimal fecha,
        BigDecimal caracteristicas) {
}
