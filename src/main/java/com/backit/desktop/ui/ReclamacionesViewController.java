package com.backit.desktop.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class ReclamacionesViewController {

    @FXML private ComboBox<String> cmbTipoEvidencia;
    @FXML private TextArea txtDescripcionEvidencia;
    @FXML private Label lblArchivoInfo;
    @FXML private TextArea txtMotivoRechazo;
    @FXML private TableView<?> tvReclamaciones;

    @FXML
    public void initialize() {
        cmbTipoEvidencia.getItems().addAll("Factura de Compra", "Fotografia Adicional", "Seña Especifica", "Firma Digital");
    }

    @FXML private void handleAdjuntarArchivo() { lblArchivoInfo.setText("factura_compra.pdf"); }
    @FXML private void handleGuardarEvidencia() { System.out.println("Evidencia guardada."); }
    @FXML private void handleAprobar() { System.out.println("Reclamacion aprobada."); }
    @FXML private void handleRechazar() { System.out.println("Reclamacion rechazada."); }
}
