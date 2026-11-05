package com.backit.desktop.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class CoincidenciasViewController {


    @FXML private TableView<?> tvCoincidencias; // cambiar '?' por tu clase DTO de Coincidencia
    @FXML private TableColumn<?, ?> colId;
    @FXML private TableColumn<?, ?> colObjeto;
    @FXML private TableColumn<?, ?> colReporte;
    @FXML private TableColumn<?, ?> colScoreTotal;
    @FXML private TableColumn<?, ?> colEstado;


    @FXML private ProgressBar pbScoreTotal;
    @FXML private Label lblScoreTotal;
    @FXML private Label lblCat;
    @FXML private Label lblUbic;
    @FXML private Label lblFecha;
    @FXML private Label lblDesc;
    @FXML private Label lblCaract;
    @FXML private Button btnIniciarReclamacion;

    @FXML
    public void initialize() {
        lblScoreTotal.setText("Puntaje Total: 0% Match");
        pbScoreTotal.setProgress(0.0);


    }

    @FXML
    private void handleIniciarReclamacion() {
        System.out.println("Reclamación formal iniciada.");
    }
}