package com.backit.desktop.matching;

import java.math.BigDecimal;

/** Desglose explicable del puntaje de una coincidencia. */
public record ResultadoMatching(
        BigDecimal categoria,
        BigDecimal descripcion,
        BigDecimal ubicacion,
        BigDecimal fecha,
        BigDecimal caracteristicas,
        BigDecimal total) {
}
