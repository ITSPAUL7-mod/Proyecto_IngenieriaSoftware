package com.backit.desktop.ui;

import com.backit.desktop.exceptions.BackItException;
import javafx.scene.control.Alert;

/** Mensajes al usuario. Las excepciones de dominio muestran su mensaje; las demás, uno genérico. */
final class Alertas {

    private Alertas() {
    }

    static void info(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "BackIt", mensaje);
    }

    static void error(Exception e) {
        if (e instanceof BackItException) {
            mostrar(Alert.AlertType.WARNING, "No se pudo completar la acción", e.getMessage());
        } else {
            e.printStackTrace(); // TODO Sprint 7: persistir en error_sistema
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }
            mostrar(Alert.AlertType.ERROR, "Error inesperado",
                    "Ocurrió un error interno: " + causa.getClass().getSimpleName()
                            + (causa.getMessage() == null ? "" : " — " + causa.getMessage()));
        }
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
