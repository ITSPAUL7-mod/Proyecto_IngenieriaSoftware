package com.backit.desktop.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class TerminosModalController {

    @FXML private Label lblVersion;
    @FXML private TextArea txtContenidoTerminos;
    @FXML private CheckBox chkAceptar;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    @FXML
    public void initialize() {
        chkAceptar.selectedProperty().addListener((obs, oldVal, newVal) -> {
            btnConfirmar.setDisable(!newVal);
        });
    }

    @FXML
    private void handleConfirmar() {
        System.out.println("Terminos aceptados por el usuario.");
    }

    @FXML
    private void handleCancelar() {
        System.out.println("Terminos rechazados.");
    }
}
