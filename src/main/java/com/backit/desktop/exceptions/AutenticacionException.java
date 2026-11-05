package com.backit.desktop.exceptions;

/** Credenciales inválidas, cuenta no disponible o ausencia de sesión. */
public class AutenticacionException extends BackItException {
    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
