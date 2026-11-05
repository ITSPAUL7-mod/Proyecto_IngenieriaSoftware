package com.backit.desktop.utils;

import com.backit.desktop.exceptions.ValidacionException;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/** Validación de entradas antes de llegar a JPA (complementa las consultas parametrizadas). */
public final class Validadores {

    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    public static final int PASSWORD_MIN = 10;
    /** BCrypt solo procesa los primeros 72 bytes: se rechaza más para no truncar en silencio. */
    public static final int PASSWORD_MAX_BYTES = 72;

    private Validadores() {
    }

    public static String texto(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo \"" + campo + "\" es obligatorio.");
        }
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw new ValidacionException("El campo \"" + campo + "\" no puede superar " + max + " caracteres.");
        }
        return limpio;
    }

    /** Devuelve el correo normalizado (minúsculas, sin espacios). */
    public static String correo(String valor) {
        String c = texto(valor, "Correo", 180).toLowerCase();
        if (!CORREO.matcher(c).matches()) {
            throw new ValidacionException("El correo no tiene un formato válido.");
        }
        return c;
    }

    public static void password(String valor) {
        if (valor == null || valor.length() < PASSWORD_MIN) {
            throw new ValidacionException("La contraseña debe tener al menos " + PASSWORD_MIN + " caracteres.");
        }
        if (valor.getBytes(StandardCharsets.UTF_8).length > PASSWORD_MAX_BYTES) {
            throw new ValidacionException("La contraseña es demasiado larga (máximo " + PASSWORD_MAX_BYTES + " bytes).");
        }
        boolean mayus = valor.chars().anyMatch(Character::isUpperCase);
        boolean minus = valor.chars().anyMatch(Character::isLowerCase);
        boolean digito = valor.chars().anyMatch(Character::isDigit);
        if (!(mayus && minus && digito)) {
            throw new ValidacionException("La contraseña debe combinar mayúsculas, minúsculas y números.");
        }
    }
}
