package com.backit.desktop.prototipo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class TerminosBosquejoController {
    @FXML private CheckBox chkAceptar;
    @FXML private Button btnConfirmar;
    private Stage dialogo;
    private boolean aceptado;

    @FXML public void initialize() {
        btnConfirmar.disableProperty().bind(chkAceptar.selectedProperty().not());
    }
    public void asociar(Stage dialogo) { this.dialogo = dialogo; }
    public boolean aceptado() { return aceptado; }

    @FXML private void handleConfirmar() {
        aceptado = true;
        System.out.println("[BackIt] Aceptacion SOLO visual; no se registra en base de datos.");
        dialogo.close();
    }
    @FXML private void handleCancelar() {
        aceptado = false;
        System.out.println("[BackIt] Usuario no acepto los terminos de demostracion.");
        dialogo.close();
    }
}
