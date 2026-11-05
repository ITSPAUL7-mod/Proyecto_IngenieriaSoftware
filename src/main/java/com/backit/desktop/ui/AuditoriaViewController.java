package com.backit.desktop.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaViewController {

    @FXML private TextField txtBuscarAuditoria;
    @FXML private TableView<?> tvAuditoria;
    @FXML private TableView<?> tvErrores;

    @FXML
    public void initialize() {
        System.out.println("Modulo de Auditoria inicializado.");
    }

    @FXML
    private void handleFiltrar() {
        System.out.println("Filtrando auditoria.");
    }
}
