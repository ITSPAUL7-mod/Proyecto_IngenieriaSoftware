package com.backit.desktop.dto;

import com.backit.desktop.security.RolNombre;

//creacion de una cuenta por un usuario autenticado
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
