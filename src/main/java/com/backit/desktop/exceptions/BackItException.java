package com.backit.desktop.exceptions;

//excepciones de dominio: su mensaje es seguro para mostrar al usuario
public class BackItException extends RuntimeException {
    public BackItException(String mensaje) {
        super(mensaje);
    }
}
