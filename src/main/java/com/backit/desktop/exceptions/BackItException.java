package com.backit.desktop.exceptions;

/** Base de las excepciones de dominio: su mensaje es seguro para mostrarse al usuario. */
public class BackItException extends RuntimeException {
    public BackItException(String mensaje) {
        super(mensaje);
    }
}
