package com.backit.desktop.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class ObjetosEncontradosViewController {

    @FXML private TextField txtTitulo;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbUbicacion;
    @FXML private DatePicker dpFechaEncontrado;
    @FXML private TextArea txtDescripcionPublica;
    @FXML private TextArea txtCaracteristicasPrivadas;
    @FXML private Label lblFotoInfo;
    @FXML private TableView<?> tvObjetos;

    @FXML
    public void initialize() {
        cmbCategoria.getItems().addAll("Electronica", "Documentos", "Prendas", "Accesorios", "Otros");
        cmbUbicacion.getItems().addAll("Edificio A - Piso 1", "Biblioteca Central", "Cafeteria Principal");
    }

    @FXML private void handleCargarFoto() { lblFotoInfo.setText("foto_hallazgo_001.jpg"); }
    @FXML private void handleGuardar() { System.out.println("Objeto guardado en custodia."); }
    @FXML private void handleGenerarQR() { System.out.println("Codigo QR generado."); }
}
