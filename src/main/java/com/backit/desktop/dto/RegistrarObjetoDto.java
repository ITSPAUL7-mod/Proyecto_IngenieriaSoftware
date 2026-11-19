package com.backit.desktop.dto;

import java.time.LocalDate;

/** Sprint 3 - datos del formulario de hallazgo. */
public record RegistrarObjetoDto(
        String titulo,
        String descripcion,
        String caracteristicasPrivadas,
        Long categoriaId,
        Long ubicacionId,
        Long institucionId,
        LocalDate fechaEncontrado) {
}
