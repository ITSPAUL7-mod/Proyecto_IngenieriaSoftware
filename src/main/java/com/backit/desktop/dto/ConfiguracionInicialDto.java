package com.backit.desktop.dto;

//Datos del asistente de primer arranque: primera institución mas la cuenta del SUPERADMIN.
public record ConfiguracionInicialDto(
        String nombreInstitucion,
        String nombre,
        String apellido,
        String correo,
        String password,
        String passwordConfirmacion) {

    @Override
    public String toString() {
        return "ConfiguracionInicialDto[institucion=" + nombreInstitucion + ", correo=" + correo + "]";
    }
}
