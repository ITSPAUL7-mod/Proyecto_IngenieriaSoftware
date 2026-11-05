package com.backit.desktop.prototipo;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class PantallasBosquejo {
    private PantallasBosquejo() { }

    public static FXMLLoader cargar(String archivo) throws IOException {
        var url = PantallasBosquejo.class.getResource("/prototipo/templates/" + archivo);
        if (url == null) throw new IOException("No se encontro la vista: " + archivo);
        return new FXMLLoader(url);
    }

    public static void mostrar(Stage stage, String archivo, String titulo) throws IOException {
        Parent raiz = cargar(archivo).load();
        stage.setScene(new Scene(raiz, archivo.equals("MainLayout.fxml") ? 1280 : 1050,
                                      archivo.equals("MainLayout.fxml") ? 740 : 680));
        stage.setTitle(titulo);
        stage.show();
    }

    public static boolean aceptarTerminos(Stage owner) throws IOException {
        FXMLLoader cargador = cargar("TerminosModal.fxml");
        Parent raiz = cargador.load();
        Stage dialogo = new Stage();
        dialogo.initOwner(owner);
        dialogo.initModality(Modality.WINDOW_MODAL);
        dialogo.setTitle("BackIt - Terminos y privacidad (bosquejo)");
        dialogo.setScene(new Scene(raiz, 690, 540));
        TerminosBosquejoController controlador = cargador.getController();
        controlador.asociar(dialogo);
        dialogo.showAndWait();
        return controlador.aceptado();
    }

    public static void aviso(String modulo, String accion) {
        String mensaje = "BOSQUEJO: " + accion + ".\n" +
                "Esta accion se representa visualmente; la logica definitiva " +
                "y la persistencia se implementaran en sprints futuros.";
        System.out.println("[BackIt / " + modulo + "] " + mensaje.replace('\n', ' '));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Prototipo inicial BackIt");
        alert.setHeaderText(modulo + " - simulacion");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static Parent modulo(String archivo) throws IOException {
        Parent raiz = cargar(archivo).load();
        ScrollPane scroll = new ScrollPane(raiz);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        return scroll;
    }
}
