package com.backit.desktop.storage;

import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.utils.Hashing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

//almacenamiento local protegido de fotos, etiquetas QR y evidencias
//como Carpetas privadas, nombre aleatorio, extension y tamaño validados y hash SHA-256.

@Service
public class AlmacenamientoService {

    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final Set<String> EXTENSIONES = Set.of("jpg", "jpeg", "png");

    public record ArchivoGuardado(String ruta, String hash) {
    }

    private final Path base;

    public AlmacenamientoService(@Value("${backit.archivos.dir}") String directorio) {
        this.base = Paths.get(directorio).toAbsolutePath().normalize();
    }

    /** Guarda una foto de hallazgo y devuelve su ruta privada y su hash SHA-256. */
    public ArchivoGuardado guardarFoto(byte[] bytes, String nombreOriginal, String prefijo) throws IOException {
        validar(bytes, nombreOriginal);
        String extension = extension(nombreOriginal);
        Path carpeta = base.resolve("fotos");
        Files.createDirectories(carpeta);
        String nombre = (prefijo == null ? "foto" : prefijo) + "_" + UUID.randomUUID() + "." + extension;
        Path destino = carpeta.resolve(nombre);
        Files.write(destino, bytes);
        return new ArchivoGuardado(destino.toString(), Hashing.sha256Hex(bytes));
    }

    public ArchivoGuardado guardarEvidencia(byte[] bytes, String nombreOriginal, String prefijo) throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new ValidacionException("No se seleccionó ningún archivo.");
        }
        if (bytes.length > MAX_BYTES) {
            throw new ValidacionException("El archivo supera el tamaño máximo de 5 MB.");
        }
        String extension = extensionEvidencia(nombreOriginal);
        Path carpeta = base.resolve("evidencias");
        Files.createDirectories(carpeta);
        String nombre = (prefijo == null ? "evidencia" : prefijo) + "_" + UUID.randomUUID() + "." + extension;
        Path destino = carpeta.resolve(nombre);
        Files.write(destino, bytes);
        return new ArchivoGuardado(destino.toString(), Hashing.sha256Hex(bytes));
    }

    private String extensionEvidencia(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            throw new ValidacionException("El archivo debe tener extensión .jpg, .jpeg, .png o .pdf.");
        }
        String ext = nombreOriginal.substring(nombreOriginal.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!Set.of("jpg", "jpeg", "png", "pdf").contains(ext)) {
            throw new ValidacionException("Formato no permitido. Use .jpg, .jpeg, .png o .pdf.");
        }
        return ext;
    }

    public String guardarEtiqueta(byte[] png, UUID codigoQr) throws IOException {
        Path carpeta = base.resolve("qr");
        Files.createDirectories(carpeta);
        Path destino = carpeta.resolve("qr_" + codigoQr + ".png");
        Files.write(destino, png);
        return destino.toString();
    }

    public byte[] leer(String ruta) throws IOException {
        return Files.readAllBytes(Paths.get(ruta));
    }

    private void validar(byte[] bytes, String nombreOriginal) {
        if (bytes == null || bytes.length == 0) {
            throw new ValidacionException("No se seleccionó ningún archivo.");
        }
        if (bytes.length > MAX_BYTES) {
            throw new ValidacionException("El archivo supera el tamaño máximo de 5 MB.");
        }
        extension(nombreOriginal);
    }

    private String extension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            throw new ValidacionException("El archivo debe tener extensión .jpg, .jpeg o .png.");
        }
        String ext = nombreOriginal.substring(nombreOriginal.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!EXTENSIONES.contains(ext)) {
            throw new ValidacionException("Formato no permitido. Use .jpg, .jpeg o .png.");
        }
        return ext;
    }
}
