package com.backit.desktop.exceptions;

// Para el acceso restringido de roles inferiores (RBAC). */
public class AccesoDenegadoException extends BackItException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
