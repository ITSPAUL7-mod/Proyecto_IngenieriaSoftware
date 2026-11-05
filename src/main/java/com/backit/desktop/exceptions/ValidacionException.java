package com.backit.desktop.exceptions;

/** Entrada inválida o regla de negocio incumplida (correo duplicado, contraseña débil, etc.). */
public class ValidacionException extends BackItException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
