package com.backit.desktop.dto;

import java.time.LocalDate;

//datos del formulario de hallazgo del objeto
public record RegistrarObjetoDto(
        String titulo,
        String descripcion,
        String caracteristicasPrivadas,
        Long categoriaId,
        Long ubicacionId,
        Long institucionId,
        LocalDate fechaEncontrado) {
}
