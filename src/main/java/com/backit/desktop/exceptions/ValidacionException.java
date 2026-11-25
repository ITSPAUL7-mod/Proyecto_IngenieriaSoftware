package com.backit.desktop.exceptions;

//Entrada inválida de datos (correo duplicado, contraseña débil)
public class ValidacionException extends BackItException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
