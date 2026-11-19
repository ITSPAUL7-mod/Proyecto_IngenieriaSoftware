package com.backit.desktop.ui;

import com.backit.desktop.dto.BuscarReportesDto;
import com.backit.desktop.dto.CrearReporteDto;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.services.CategoriaService;
import com.backit.desktop.services.InstitucionService;
import com.backit.desktop.services.ReportePerdidaService;
import com.backit.desktop.services.UbicacionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import model.operativo.CategoriaObjeto;
import model.operativo.ReportePerdida;
import model.operativo.Ubicacion;
import model.seguridad.Institucion;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/** Sprint 4 - alta de reportes y busqueda real por atributos. */
@Component
public class ReportePerdidaViewController {

    @FXML private TextField txtTituloPerdida;
    @FXML private ComboBox<CategoriaObjeto> cmbCategoriaPerdida;
    @FXML private ComboBox<Ubicacion> cmbUbicacionPerdida;
    @FXML private DatePicker dpFechaPerdida;
    @FXML private TextArea txtDescripcionPerdida;
    @FXML private TextArea txtSenasReservadas;

    @FXML private TextField txtBuscarCatalogo;
    @FXML private ComboBox<CategoriaObjeto> cmbFiltroCategoria;
    @FXML private TableView<ReportePerdida> tvCatalogo;
    @FXML private ComboBox<Institucion> cmbInstitucion;

    private final ReportePerdidaService reporteService;
    private final CategoriaService categoriaService;
    private final UbicacionService ubicacionService;
    private final InstitucionService institucionService;
    private final SesionActual sesion;

    public ReportePerdidaViewController(ReportePerdidaService reporteService,
                                        CategoriaService categoriaService,
                                        UbicacionService ubicacionService,
                                        InstitucionService institucionService,
                                        SesionActual sesion) {
        this.reporteService = reporteService;
        this.categoriaService = categoriaService;
        this.ubicacionService = ubicacionService;
        this.institucionService = institucionService;
        this.sesion = sesion;
    }

    @FXML
    public void initialize() {
        StringConverter<CategoriaObjeto> convCategoria = new StringConverter<>() {
            public String toString(CategoriaObjeto c) { return c == null ? "" : c.getNombre(); }
            public CategoriaObjeto fromString(String s) { return null; }
        };
        StringConverter<Ubicacion> convUbicacion = new StringConverter<>() {
            public String toString(Ubicacion u) { return u == null ? "" : u.getNombre(); }
            public Ubicacion fromString(String s) { return null; }
        };
        cmbCategoriaPerdida.setConverter(convCategoria);
        cmbFiltroCategoria.setConverter(convCategoria);
        cmbUbicacionPerdida.setConverter(convUbicacion);
        cmbInstitucion.setConverter(new StringConverter<>() {
            public String toString(Institucion i) { return i == null ? "" : i.getNombre(); }
            public Institucion fromString(String s) { return null; }
        });

        try {
            boolean global = sesion.usuario().map(u -> u.getInstitucion() == null).orElse(true);
            var categorias = categoriaService.listarActivas();
            cmbCategoriaPerdida.getItems().setAll(categorias);
            cmbFiltroCategoria.getItems().setAll(categorias);
            cmbInstitucion.getItems().setAll(institucionService.visibles());
            if (global) {
                cmbInstitucion.setPromptText("Seleccione la institución");
            } else {
                cmbInstitucion.getSelectionModel().selectFirst();
                cmbInstitucion.setVisible(false);
                cmbInstitucion.setManaged(false);
            }
            cmbInstitucion.getSelectionModel().selectedItemProperty().addListener(
                    (obs, antes, ahora) -> recargarUbicaciones());
            recargarUbicaciones();
        } catch (Exception e) {
            Alertas.error(e);
        }

        configurarColumnas();
        handleBuscar();
    }

    private Long institucionId() {
        return cmbInstitucion.getValue() == null ? null : cmbInstitucion.getValue().getId();
    }

    private void recargarUbicaciones() {
        try {
            cmbUbicacionPerdida.getItems().setAll(ubicacionService.visiblesDe(institucionId()));
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    private void configurarColumnas() {
        var columnas = tvCatalogo.getColumns();
        if (columnas.size() < 5) {
            return;
        }
        setTexto(columnas.get(0), ReportePerdida::getTitulo);
        setTexto(columnas.get(1), r -> r.getCategoria() == null ? "" : r.getCategoria().getNombre());
        setTexto(columnas.get(2), r -> r.getUbicacionPerdida() == null ? "" : r.getUbicacionPerdida().getNombre());
        setTexto(columnas.get(3), r -> r.getFechaPerdida() == null ? "" :
                DateTimeFormatter.ofPattern("dd/MM/yyyy").format(r.getFechaPerdida()));
        // Solo datos publicos: nunca se muestra caracteristicasPrivadas.
        setTexto(columnas.get(4), ReportePerdida::getDescripcion);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setTexto(javafx.scene.control.TableColumn columna,
                                 java.util.function.Function<ReportePerdida, String> extractor) {
        columna.setCellValueFactory(d -> new SimpleStringProperty(extractor.apply(
                (ReportePerdida) ((javafx.scene.control.TableColumn.CellDataFeatures) d).getValue())));
    }

    @FXML
    private void handleRegistrarPerdida() {
        try {
            ReportePerdida r = reporteService.crear(new CrearReporteDto(
                    txtTituloPerdida.getText(),
                    txtDescripcionPerdida.getText(),
                    txtSenasReservadas.getText(),
                    cmbCategoriaPerdida.getValue() == null ? null : cmbCategoriaPerdida.getValue().getId(),
                    cmbUbicacionPerdida.getValue() == null ? null : cmbUbicacionPerdida.getValue().getId(),
                    institucionId(),
                    dpFechaPerdida.getValue()));
            Alertas.info("Reporte de pérdida registrado (ID " + r.getId() + ").");
            txtTituloPerdida.clear();
            txtDescripcionPerdida.clear();
            txtSenasReservadas.clear();
            dpFechaPerdida.setValue(null);
            handleBuscar();
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    @FXML
    private void handleBuscar() {
        try {
            tvCatalogo.getItems().setAll(reporteService.buscar(new BuscarReportesDto(
                    txtBuscarCatalogo.getText(),
                    cmbFiltroCategoria.getValue() == null ? null : cmbFiltroCategoria.getValue().getId(),
                    null, null, null)));
        } catch (Exception e) {
            Alertas.error(e);
        }
    }
}
