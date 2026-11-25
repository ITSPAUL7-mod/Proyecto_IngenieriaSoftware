package com.backit.desktop.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/** generacion y lectura de QR con ZXing.
 * El contenido del QR es solo del token UUID: nunca datos privados ni el ID secuencial.
 */
@Service
public class QrService {

    public byte[] generarPng(UUID codigo, int tamano) throws IOException {
        BitMatrix matriz;
        try {
            matriz = new QRCodeWriter().encode(codigo.toString(), BarcodeFormat.QR_CODE, tamano, tamano);
        } catch (com.google.zxing.WriterException e) {
            throw new IOException("No se pudo codificar el QR.", e);
        }
        BufferedImage imagen = MatrixToImageWriter.toBufferedImage(matriz);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, "PNG", salida);
        return salida.toByteArray();
    }

    public Optional<UUID> leer(byte[] png) {
        try {
            BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(png));
            if (imagen == null) {
                return Optional.empty();
            }
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(imagen)));
            Result resultado = new QRCodeReader().decode(bitmap);
            return Optional.of(UUID.fromString(resultado.getText()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
