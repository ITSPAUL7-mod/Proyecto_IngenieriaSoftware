package com.backit.desktop.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class EntregaFisicaViewController {

    @FXML private TextField txtCodigoQR;
    @FXML private Label lblObjetoInfo;
    @FXML private Label lblReclamanteInfo;
    @FXML private Button btnConfirmarEntrega;

    @FXML private Slider sliderCalificacion;
    @FXML private TextArea txtComentarioValoracion;

    @FXML
    private void handleValidarQR() {
        lblObjetoInfo.setText("Objeto: Laptop Dell XPS 15 (ID: 104)");
        lblReclamanteInfo.setText("Reclamante: Carlos Mendoza (CI: 17263849)");
        btnConfirmarEntrega.setDisable(false);
    }

    @FXML
    private void handleConfirmarEntrega() {
        System.out.println("Entrega fisica registrada. Estado cambiado a ENTREGADO.");
    }

    @FXML
    private void handleEnviarValoracion() {
        System.out.println("Valoracion registrada: " + sliderCalificacion.getValue() + " estrellas.");
    }
}
