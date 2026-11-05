package com.backit.desktop.dto;

public record CrearInstitucionDto(
        String nombre,
        String ruc,
        String direccion,
        String telefono,
        String contactoPrincipal) {
}
