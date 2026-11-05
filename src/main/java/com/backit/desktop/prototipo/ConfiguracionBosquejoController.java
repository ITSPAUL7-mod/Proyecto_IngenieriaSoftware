package com.backit.desktop.prototipo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ConfiguracionBosquejoController {
    @FXML private TextField txtInstitucion;
    @FXML private Button btnCrear;

    @FXML private void handleCrear() {
        PantallasBosquejo.aviso("Configuracion inicial",
                "Alta prevista de institucion " +
                (txtInstitucion.getText().isBlank() ? "de ejemplo" : txtInstitucion.getText()) +
                " y cuenta SUPERADMIN");
        try {
            PantallasBosquejo.mostrar((Stage) btnCrear.getScene().getWindow(),
                    "LoginView.fxml", "BackIt - Acceso simulado");
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
