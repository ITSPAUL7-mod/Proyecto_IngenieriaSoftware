package com.backit.desktop.ui;

import com.backit.desktop.dto.CoincidenciaDto;
import com.backit.desktop.dto.CrearReclamacionDto;
import com.backit.desktop.matching.MotorCoincidenciasService;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.services.CoincidenciaService;
import com.backit.desktop.services.ReclamacionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.reclamacion.Reclamacion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Component
public class CoincidenciasViewController {

    private static final DecimalFormat PORCENTAJE = new DecimalFormat("0.00");

    @FXML private TableView<CoincidenciaDto> tvCoincidencias;
    @FXML private TableColumn<CoincidenciaDto, Long> colId;
    @FXML private TableColumn<CoincidenciaDto, String> colObjeto;
    @FXML private TableColumn<CoincidenciaDto, String> colReporte;
    @FXML private TableColumn<CoincidenciaDto, BigDecimal> colScoreTotal;
    @FXML private TableColumn<CoincidenciaDto, String> colEstado;

    @FXML private ProgressBar pbScoreTotal;
    @FXML private Label lblScoreTotal;
    @FXML private Label lblCat;
    @FXML private Label lblUbic;
    @FXML private Label lblFecha;
    @FXML private Label lblDesc;
    @FXML private Label lblCaract;
    @FXML private Button btnIniciarReclamacion;
    @FXML private Button btnRecalcular;

    private final CoincidenciaService coincidenciaService;
    private final ReclamacionService reclamacionService;
    private final MotorCoincidenciasService motor;
    private final SesionActual sesion;

    public CoincidenciasViewController(CoincidenciaService coincidenciaService,
                                       ReclamacionService reclamacionService,
                                       MotorCoincidenciasService motor,
                                       SesionActual sesion) {
        this.coincidenciaService = coincidenciaService;
        this.reclamacionService = reclamacionService;
        this.motor = motor;
        this.sesion = sesion;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleLongProperty(
                d.getValue().id() == null ? 0 : d.getValue().id()).asObject());
        colObjeto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().objetoTitulo()));
        colReporte.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().reporteTitulo()));
        colScoreTotal.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().total()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado()));

        tvCoincidencias.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> mostrarDesglose(seleccionado));
        btnIniciarReclamacion.setDisable(true);
        btnRecalcular.setDisable(!sesion.tienePermiso(PermisoCodigo.COINCIDENCIA_REVISAR));
        recargar();
    }

    private void recargar() {
        try {
            tvCoincidencias.getItems().setAll(coincidenciaService.listarVisibles());
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    private void mostrarDesglose(CoincidenciaDto c) {
        if (c == null) {
            lblScoreTotal.setText("Puntaje Total: 0% Match");
            pbScoreTotal.setProgress(0);
            lblCat.setText("Categoría: 0.0");
            lblUbic.setText("Ubicación: 0.0");
            lblFecha.setText("Fecha: 0.0");
            lblDesc.setText("Descripción: 0.0");
            lblCaract.setText("Señas Privadas: 0.0");
            btnIniciarReclamacion.setDisable(true);
            return;
        }
        lblScoreTotal.setText("Puntaje Total: " + PORCENTAJE.format(c.total()) + "% Match (" + c.estado() + ")");
        pbScoreTotal.setProgress(c.total().doubleValue() / 100.0);
        lblCat.setText("Categoría: " + PORCENTAJE.format(c.categoria()));
        lblUbic.setText("Ubicación: " + PORCENTAJE.format(c.ubicacion()));
        lblFecha.setText("Fecha: " + PORCENTAJE.format(c.fecha()));
        lblDesc.setText("Descripción: " + PORCENTAJE.format(c.descripcion()));
        lblCaract.setText("Señas Privadas: " + PORCENTAJE.format(c.caracteristicas()));
        btnIniciarReclamacion.setDisable(
                !sesion.tienePermiso(PermisoCodigo.RECLAMACION_CREAR) || "RECLAMADA".equals(c.estado()));
    }

    /** El dueno del reporte convierte la sugerencia en una reclamacion formal (Sprint 6). */
    @FXML
    private void handleIniciarReclamacion() {
        CoincidenciaDto seleccionada = tvCoincidencias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }
        if (!sesion.tienePermiso(PermisoCodigo.RECLAMACION_CREAR)) {
            Alertas.info("Solo el usuario dueño del reporte puede iniciar la reclamación.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("BackIt");
        confirmacion.setHeaderText("Iniciar reclamación formal");
        confirmacion.setContentText("¿Reclamar el objeto de la coincidencia #" + seleccionada.id() + "?");
        if (confirmacion.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }
        try {
            Reclamacion r = reclamacionService.crear(
                    new CrearReclamacionDto(seleccionada.id()), null, null, null, null);
            Alertas.info("Reclamación #" + r.getId() + " creada.\n"
                    + "Añada sus evidencias en el módulo «Reclamaciones».");
            recargar();
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    @FXML
    private void handleRecalcular() {
        try {
            var r = motor.recalcularTodo();
            Alertas.info("Objetos en custodia: " + r.objetos()
                    + "\nReportes activos: " + r.reportes()
                    + "\nPares comparados (misma sede): " + r.paresEvaluados()
                    + "\nNuevas coincidencias: " + r.creadas());
            recargar();
        } catch (Exception e) {
            Alertas.error(e);
        }
    }
}
