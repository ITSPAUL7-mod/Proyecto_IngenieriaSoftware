package com.backit.desktop.prototipo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainLayoutBosquejoController {
    @FXML private StackPane contentArea;
    @FXML private Label lblUsuarioInfo;
    @FXML private Label lblEstadoConexion;
    @FXML private Label lblHuellaDispositivo;

    @FXML public void initialize() {
        lblUsuarioInfo.setText("Perfil de muestra: SUPERADMIN (sin autenticacion real)");
        lblEstadoConexion.setText("PROTOTIPO: PostgreSQL NO conectado");
        lblHuellaDispositivo.setText("Solo demostracion: funciones futuras");
    }

    private void abrir(String archivo) {
        try {
            contentArea.getChildren().setAll(PantallasBosquejo.modulo(archivo));
            System.out.println("[BackIt] Vista de demostracion cargada: " + archivo);
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al abrir " + archivo + ": " + ex.getMessage()).showAndWait();
        }
    }
    @FXML private void navObjetosEncontrados() { abrir("ObjetosEncontradosView.fxml"); }
    @FXML private void navReportePerdida() { abrir("ReportePerdidaView.fxml"); }
    @FXML private void navCoincidencias() { abrir("CoincidenciasView.fxml"); }
    @FXML private void navReclamaciones() { abrir("ReclamacionesView.fxml"); }
    @FXML private void navEntregaFisica() { abrir("EntregaFisicaView.fxml"); }
    @FXML private void navGestionUsuarios() { abrir("GestionUsuariosView.fxml"); }
    @FXML private void navGestionUbicaciones() { abrir("GestionUbicacionesView.fxml"); }
    @FXML private void navNotificaciones() { abrir("NotificacionesView.fxml"); }
    @FXML private void navHistorial() { abrir("HistorialView.fxml"); }
    @FXML private void navReportes() { abrir("ReportesView.fxml"); }
    @FXML private void navConfiguracion() { abrir("ConfiguracionView.fxml"); }
    @FXML private void navAuditoria() { abrir("AuditoriaView.fxml"); }

    @FXML private void handleCerrarSesion() {
        try {
            PantallasBosquejo.mostrar((Stage) contentArea.getScene().getWindow(),
                    "LoginView.fxml", "BackIt - Acceso simulado");
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
