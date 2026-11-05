package com.backit.desktop.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class ReportePerdidaViewController {

    @FXML private TextField txtTituloPerdida;
    @FXML private ComboBox<String> cmbCategoriaPerdida;
    @FXML private ComboBox<String> cmbUbicacionPerdida;
    @FXML private DatePicker dpFechaPerdida;
    @FXML private TextArea txtDescripcionPerdida;
    @FXML private TextArea txtSenasReservadas;

    @FXML private TextField txtBuscarCatalogo;
    @FXML private ComboBox<String> cmbFiltroCategoria;

    @FXML
    public void initialize() {
        cmbCategoriaPerdida.getItems().addAll("Electronica", "Documentos", "Prendas", "Accesorios");
        cmbUbicacionPerdida.getItems().addAll("Campus Principal", "Edificio B", "Canchas Deportivas");
    }

    @FXML private void handleRegistrarPerdida() { System.out.println("Reporte registrado."); }
    @FXML private void handleBuscar() { System.out.println("Busqueda ejecutada."); }
}
