package com.backit.desktop.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class Hashing {

    private Hashing() {
    }

    /** SHA-256 en hexadecimal minúscula (64 caracteres), como exige el diseño de BD. */
    public static String sha256Hex(String texto) {
        return sha256Hex(texto.getBytes(StandardCharsets.UTF_8));
    }

    /** Hash de bytes arbitrarios (fotos, evidencias, QR). */
    public static String sha256Hex(byte[] datos) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(datos));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}
