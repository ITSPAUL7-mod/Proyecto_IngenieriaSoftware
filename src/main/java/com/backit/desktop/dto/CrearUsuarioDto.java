package com.backit.desktop.dto;

import com.backit.desktop.security.RolNombre;

/** Alta de una cuenta por un usuario autenticado. institucionId es nulo para roles globales. */
public record CrearUsuarioDto(
        String nombre,
        String apellido,
        String correo,
        String password,
        RolNombre rol,
        Long institucionId) {

    @Override
    public String toString() {
        // Evita que la contraseña aparezca en logs si alguien imprime el DTO.
        return "CrearUsuarioDto[correo=" + correo + ", rol=" + rol + ", institucionId=" + institucionId + "]";
    }
}
