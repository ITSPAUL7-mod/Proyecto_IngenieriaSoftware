package com.backit.desktop.ui;

import com.backit.desktop.security.SesionActual;
import com.backit.desktop.services.TerminoCondicionService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import model.sistema.TerminoCondicion;
import org.springframework.stereotype.Component;

/** Sprint 1 - ventana bloqueante de aceptacion versionada de Terminos y Condiciones. */
@Component
public class TerminosModalController {

    @FXML private Label lblVersion;
    @FXML private TextArea txtContenidoTerminos;
    @FXML private CheckBox chkAceptar;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    private final TerminoCondicionService terminosService;
    private final SesionActual sesion;

    private Runnable alAceptar = () -> { };
    private Runnable alRechazar = () -> { };
    private TerminoCondicion termino;

    public TerminosModalController(TerminoCondicionService terminosService, SesionActual sesion) {
        this.terminosService = terminosService;
        this.sesion = sesion;
    }

    public void alAceptar(Runnable accion) {
        this.alAceptar = accion;
    }

    public void alRechazar(Runnable accion) {
        this.alRechazar = accion;
    }

    @FXML
    public void initialize() {
        chkAceptar.setSelected(false); // nunca preaceptada
        btnConfirmar.setDisable(true);
        chkAceptar.selectedProperty().addListener((obs, antes, ahora) -> btnConfirmar.setDisable(!ahora));
        try {
            termino = terminosService.vigente();
            lblVersion.setText("Versión vigente: " + termino.getVersion());
            txtContenidoTerminos.setText(termino.getContenido());
        } catch (Exception e) {
            lblVersion.setText("No se pudo cargar la versión vigente");
            txtContenidoTerminos.setText("Contacte al administrador: " + e.getMessage());
            btnConfirmar.setDisable(true);
        }
    }

    @FXML
    private void handleConfirmar() {
        if (!chkAceptar.isSelected() || termino == null) {
            return;
        }
        try {
            terminosService.registrarDecision(sesion.requerirUsuario(), true);
            cerrar();
            alAceptar.run();
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    @FXML
    private void handleCancelar() {
        rechazar();
    }

    /** Tambien se invoca cuando el usuario cierra la ventana con la X. */
    public void rechazar() {
        try {
            terminosService.registrarDecision(sesion.requerirUsuario(), false);
        } catch (Exception e) {
            Alertas.error(e);
        }
        cerrar();
        alRechazar.run();
    }

    private void cerrar() {
        Stage stage = (Stage) btnConfirmar.getScene().getWindow();
        stage.close();
    }
}
