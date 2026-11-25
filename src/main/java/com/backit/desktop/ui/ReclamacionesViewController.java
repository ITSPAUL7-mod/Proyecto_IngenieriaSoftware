package com.backit.desktop.ui;

import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.services.ReclamacionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import model.reclamacion.Reclamacion;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;

@Component
public class ReclamacionesViewController {

    @FXML private ComboBox<String> cmbTipoEvidencia;
    @FXML private TextArea txtDescripcionEvidencia;
    @FXML private Label lblArchivoInfo;
    @FXML private TextArea txtMotivoRechazo;
    @FXML private TableView<Reclamacion> tvReclamaciones;
    @FXML private Button btnAdjuntarArchivo;
    @FXML private Button btnGuardarEvidencia;
    @FXML private Button btnAprobar;
    @FXML private Button btnRechazar;

    private final ReclamacionService reclamacionService;
    private final SesionActual sesion;

    private byte[] archivoBytes;
    private String archivoNombre;

    public ReclamacionesViewController(ReclamacionService reclamacionService, SesionActual sesion) {
        this.reclamacionService = reclamacionService;
        this.sesion = sesion;
    }

    @FXML
    public void initialize() {
        cmbTipoEvidencia.getItems().addAll("FACTURA", "FOTO", "SEÑA", "FIRMA", "OTRA");
        configurarColumnas();
        tvReclamaciones.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionada) -> actualizarBotones(seleccionada));

        boolean puedeResolver = sesion.tienePermiso(PermisoCodigo.RECLAMACION_RESOLVER);
        btnAprobar.setDisable(!puedeResolver);
        btnRechazar.setDisable(!puedeResolver);
        txtMotivoRechazo.setDisable(!puedeResolver);
        recargar();
    }

    private void configurarColumnas() {
        var columnas = tvReclamaciones.getColumns();
        if (columnas.size() < 5) {
            return;
        }
        setTexto(columnas.get(0), r -> r.getId() == null ? "" : String.valueOf(r.getId()));
        setTexto(columnas.get(1), r -> r.getUsuarioSolicitante() == null ? "" :
                r.getUsuarioSolicitante().getNombre() + " " + r.getUsuarioSolicitante().getApellido());
        setTexto(columnas.get(2), r -> r.getCoincidencia() == null
                || r.getCoincidencia().getObjetoEncontrado() == null ? "" :
                r.getCoincidencia().getObjetoEncontrado().getTitulo());
        setTexto(columnas.get(3), Reclamacion::getEstado);
        setTexto(columnas.get(4), r -> {
            String hash = r.getDocumentoVerificadoHash();
            return hash == null ? "" : hash.substring(0, Math.min(12, hash.length())) + "…";
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setTexto(javafx.scene.control.TableColumn columna,
                                 java.util.function.Function<Reclamacion, String> extractor) {
        columna.setCellValueFactory(d -> new SimpleStringProperty(extractor.apply(
                (Reclamacion) ((javafx.scene.control.TableColumn.CellDataFeatures) d).getValue())));
    }

    private void actualizarBotones(Reclamacion r) {
        boolean seleccionada = r != null;
        boolean pendiente = seleccionada && "PENDIENTE".equals(r.getEstado());
        boolean puedeResolver = sesion.tienePermiso(PermisoCodigo.RECLAMACION_RESOLVER);
        btnGuardarEvidencia.setDisable(!seleccionada || archivoBytes == null);
        btnAprobar.setDisable(!pendiente || !puedeResolver);
        btnRechazar.setDisable(!pendiente || !puedeResolver);
    }

    private void recargar() {
        try {
            tvReclamaciones.getItems().setAll(reclamacionService.listarVisibles());
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    @FXML
    private void handleAdjuntarArchivo() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Adjuntar evidencia de propiedad");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    "Evidencias", "*.jpg", "*.jpeg", "*.png", "*.pdf"));
            File archivo = chooser.showOpenDialog(btnAdjuntarArchivo.getScene().getWindow());
            if (archivo != null) {
                archivoBytes = Files.readAllBytes(archivo.toPath());
                archivoNombre = archivo.getName();
                lblArchivoInfo.setText(archivoNombre);
                actualizarBotones(tvReclamaciones.getSelectionModel().getSelectedItem());
            }
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    @FXML
    private void handleGuardarEvidencia() {
        Reclamacion seleccionada = tvReclamaciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null || archivoBytes == null) {
            Alertas.info("Seleccione una reclamación y adjunte un archivo primero.");
            return;
        }
        try {
            reclamacionService.agregarEvidencia(seleccionada.getId(), archivoBytes, archivoNombre,
                    cmbTipoEvidencia.getValue(), txtDescripcionEvidencia.getText());
            Alertas.info("Evidencia guardada con hash SHA-256.");
            archivoBytes = null;
            archivoNombre = null;
            lblArchivoInfo.setText("Ningun archivo");
            txtDescripcionEvidencia.clear();
            actualizarBotones(seleccionada);
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    @FXML
    private void handleAprobar() {
        Reclamacion seleccionada = tvReclamaciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }
        try {
            reclamacionService.aprobar(seleccionada.getId());
            Alertas.info("Reclamación aprobada. El objeto pasó a estado RECLAMADO.");
            recargar();
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    @FXML
    private void handleRechazar() {
        Reclamacion seleccionada = tvReclamaciones.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }
        try {
            reclamacionService.rechazar(seleccionada.getId(), txtMotivoRechazo.getText());
            Alertas.info("Reclamación rechazada y notificada al solicitante.");
            txtMotivoRechazo.clear();
            recargar();
        } catch (Exception e) {
            Alertas.error(e);
        }
    }
}
