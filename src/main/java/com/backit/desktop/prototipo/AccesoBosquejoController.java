package com.backit.desktop.prototipo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AccesoBosquejoController {
    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;

    @FXML private void handleLogin() {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            String alias = txtCorreo.getText().isBlank() ? "demo@backit.edu" : txtCorreo.getText();
            System.out.println("[BackIt] Acceso SIMULADO de " + alias + " (sin verificar credenciales).");
            if (PantallasBosquejo.aceptarTerminos(stage)) {
                PantallasBosquejo.mostrar(stage, "MainLayout.fxml", "BackIt - BOSQUEJO inicial");
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    @FXML private void handleVerConfiguracion() {
        try {
            PantallasBosquejo.mostrar((Stage) btnLogin.getScene().getWindow(),
                    "SetupInicialView.fxml", "BackIt - Configuracion (simulada)");
        } catch (Exception ex) { error(ex); }
    }

    private void error(Exception ex) {
        ex.printStackTrace();
        new Alert(Alert.AlertType.ERROR, "No se pudo abrir la pantalla: " + ex.getMessage()).showAndWait();
    }
}
