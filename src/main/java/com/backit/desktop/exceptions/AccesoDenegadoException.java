package com.backit.desktop.exceptions;

/** El usuario autenticado no tiene permiso para la operación (RBAC aplicado en servicios). */
public class AccesoDenegadoException extends BackItException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
