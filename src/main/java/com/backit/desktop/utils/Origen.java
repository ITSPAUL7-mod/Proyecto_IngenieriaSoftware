package com.backit.desktop.utils;

import java.net.InetAddress;

/** Valor para las columnas "origen" (host/sesión) de auditoría y errores. */
public final class Origen {

    private static final String ACTUAL = resolver();

    private Origen() {
    }

    public static String actual() {
        return ACTUAL;
    }

    private static String resolver() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "desconocido";
        }
    }
}
