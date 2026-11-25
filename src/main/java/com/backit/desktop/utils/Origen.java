package com.backit.desktop.utils;

import java.net.InetAddress;

//para saber como esta logeado el usuario
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
