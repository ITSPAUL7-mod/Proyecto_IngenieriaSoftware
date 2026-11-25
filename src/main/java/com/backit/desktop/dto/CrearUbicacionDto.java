package com.backit.desktop.dto;

//creacion de la ubicacion
public record CrearUbicacionDto(
        String nombre,
        String tipo,
        String direccion,
        String referencia,
        Long ubicacionPadreId,
        String horarioAtencion,
        Long institucionId) {
}
