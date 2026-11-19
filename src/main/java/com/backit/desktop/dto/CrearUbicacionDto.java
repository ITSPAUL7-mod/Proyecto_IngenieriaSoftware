package com.backit.desktop.dto;

/** Alta de una ubicacion jerarquica y, opcionalmente, de su punto de entrega. */
public record CrearUbicacionDto(
        String nombre,
        String tipo,
        String direccion,
        String referencia,
        Long ubicacionPadreId,
        String horarioAtencion,
        Long institucionId) {
}
