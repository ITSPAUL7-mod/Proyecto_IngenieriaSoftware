package com.backit.desktop.ui;

import com.backit.desktop.dto.RegistrarObjetoDto;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.services.CategoriaService;
import com.backit.desktop.services.InstitucionService;
import com.backit.desktop.services.ObjetoEncontradoService;
import com.backit.desktop.services.UbicacionService;
import com.backit.desktop.storage.AlmacenamientoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import model.operativo.CategoriaObjeto;
import model.operativo.ObjetoEncontrado;
import model.operativo.Ubicacion;
import model.seguridad.Institucion;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;

/** Sprint 3 - registro real de hallazgos (antes solo imprimia por consola). */
@Component
public class ObjetosEncontradosViewController {

    @FXML private TextField txtTitulo;
    @FXML private ComboBox<CategoriaObjeto> cmbCategoria;
    @FXML private ComboBox<Ubicacion> cmbUbicacion;
    @FXML private DatePicker dpFechaEncontrado;
    @FXML private TextArea txtDescripcionPublica;
    @FXML private TextArea txtCaracteristicasPrivadas;
    @FXML private Label lblFotoInfo;
    @FXML private Button btnCargarFoto;
    @FXML private Button btnGenerarQR;
    @FXML private TableView<ObjetoEncontrado> tvObjetos;
    @FXML private TableColumn<ObjetoEncontrado, Long> colId;
    @FXML private TableColumn<ObjetoEncontrado, String> colTitulo;
    @FXML private TableColumn<ObjetoEncontrado, String> colCategoria;
    @FXML private TableColumn<ObjetoEncontrado, String> colUbicacion;
    @FXML private TableColumn<ObjetoEncontrado, String> colFecha;
    @FXML private TableColumn<ObjetoEncontrado, String> colEstado;
    @FXML private ComboBox<Institucion> cmbInstitucion;

    private final ObjetoEncontradoService objetoService;
    private final CategoriaService categoriaService;
    private final UbicacionService ubicacionService;
    private final InstitucionService institucionService;
    private final SesionActual sesion;
    private final AlmacenamientoService almacenamiento;

    private byte[] fotoBytes;
    private String fotoNombre;

    public ObjetosEncontradosViewController(ObjetoEncontradoService objetoService,
                                            CategoriaService categoriaService,
                                            UbicacionService ubicacionService,
                                            InstitucionService institucionService,
                                            SesionActual sesion,
                                            AlmacenamientoService almacenamiento) {
        this.objetoService = objetoService;
        this.categoriaService = categoriaService;
        this.ubicacionService = ubicacionService;
        this.institucionService = institucionService;
        this.sesion = sesion;
        this.almacenamiento = almacenamiento;
    }

    @FXML
    public void initialize() {
        cmbCategoria.setConverter(new StringConverter<>() {
            public String toString(CategoriaObjeto c) { return c == null ? "" : c.getNombre(); }
            public CategoriaObjeto fromString(String s) { return null; }
        });
        cmbUbicacion.setConverter(new StringConverter<>() {
            public String toString(Ubicacion u) { return u == null ? "" : u.getNombre() + " (" + u.getTipo() + ")"; }
            public Ubicacion fromString(String s) { return null; }
        });
        cmbInstitucion.setConverter(new StringConverter<>() {
            public String toString(Institucion i) { return i == null ? "" : i.getNombre(); }
            public Institucion fromString(String s) { return null; }
        });

        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleLongProperty(
                d.getValue().getId() == null ? 0 : d.getValue().getId()).asObject());
        colTitulo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitulo()));
        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCategoria() == null ? "" : d.getValue().getCategoria().getNombre()));
        colUbicacion.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getUbicacion() == null ? "" : d.getValue().getUbicacion().getNombre()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaEncontrado() == null ? "" :
                        DateTimeFormatter.ofPattern("dd/MM/yyyy").format(d.getValue().getFechaEncontrado())));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));

        try {
            boolean global = sesion.usuario().map(u -> u.getInstitucion() == null).orElse(true);
            cmbInstitucion.getItems().setAll(institucionService.visibles());
            if (global) {
                cmbInstitucion.setPromptText("Seleccione la institución");
            } else {
                cmbInstitucion.getSelectionModel().selectFirst();
                cmbInstitucion.setVisible(false);
                cmbInstitucion.setManaged(false);
            }
            cmbInstitucion.getSelectionModel().selectedItemProperty().addListener(
                    (obs, antes, ahora) -> recargarCombos());
        } catch (Exception e) {
            Alertas.error(e);
        }

        recargarCombos();
        recargarTabla();
    }

    private Long institucionId() {
        return cmbInstitucion.getValue() == null ? null : cmbInstitucion.getValue().getId();
    }

    private void recargarCombos() {
        try {
            cmbCategoria.getItems().setAll(categoriaService.listarActivas());
            cmbUbicacion.getItems().setAll(ubicacionService.visiblesDe(institucionId()));
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    private void recargarTabla() {
        try {
            tvObjetos.getItems().setAll(objetoService.listarVisibles());
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    @FXML
    private void handleCargarFoto() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Seleccionar foto del hallazgo");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.jpeg", "*.png"));
            File archivo = chooser.showOpenDialog(btnCargarFoto.getScene().getWindow());
            if (archivo != null) {
                fotoBytes = Files.readAllBytes(archivo.toPath());
                fotoNombre = archivo.getName();
                lblFotoInfo.setText(fotoNombre);
            }
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    @FXML
    private void handleGuardar() {
        ObjetoEncontrado objeto;
        try {
            objeto = objetoService.registrar(new RegistrarObjetoDto(
                    txtTitulo.getText(),
                    txtDescripcionPublica.getText(),
                    txtCaracteristicasPrivadas.getText(),
                    cmbCategoria.getValue() == null ? null : cmbCategoria.getValue().getId(),
                    cmbUbicacion.getValue() == null ? null : cmbUbicacion.getValue().getId(),
                    institucionId(),
                    dpFechaEncontrado.getValue()), fotoBytes, fotoNombre);
        } catch (Exception e) {
            Alertas.error(e);
            return;
        }

        txtTitulo.clear();
        txtDescripcionPublica.clear();
        txtCaracteristicasPrivadas.clear();
        dpFechaEncontrado.setValue(null);
        fotoBytes = null;
        fotoNombre = null;
        lblFotoInfo.setText("Sin foto seleccionada");
        recargarTabla();
        generarEtiqueta(objeto, true);
    }

    @FXML
    private void handleGenerarQR() {
        ObjetoEncontrado seleccionado = tvObjetos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.info("Seleccione un objeto de la tabla para generar su etiqueta QR.");
            return;
        }
        generarEtiqueta(seleccionado, false);
    }

    private void generarEtiqueta(ObjetoEncontrado objeto, boolean recienRegistrado) {
        try {
            byte[] png = objetoService.etiquetaQr(objeto.getId());
            String ruta = almacenamiento.guardarEtiqueta(png, objeto.getCodigoQr());

            ImageView vista = new ImageView(new Image(new ByteArrayInputStream(png)));
            vista.setAccessibleText("Código QR del objeto " + objeto.getTitulo());
            TextField codigo = new TextField(objeto.getCodigoQr().toString());
            codigo.setEditable(false);
            TextField archivo = new TextField(ruta);
            archivo.setEditable(false);

            Alert dialogo = new Alert(Alert.AlertType.INFORMATION);
            dialogo.initOwner(btnGenerarQR.getScene().getWindow());
            dialogo.setTitle("Etiqueta QR");
            dialogo.setHeaderText(recienRegistrado ? "Objeto registrado en custodia" : "Etiqueta QR generada");
            dialogo.getDialogPane().setContent(new VBox(8, new Label("Objeto #" + objeto.getId()
                    + " · " + objeto.getTitulo()), vista, new Label("Código del objeto"), codigo,
                    new Label("Archivo PNG guardado en"), archivo));
            dialogo.showAndWait();
        } catch (Exception e) {
            if (recienRegistrado) {
                Alertas.info("El objeto #" + objeto.getId() + " quedó registrado en custodia. "
                        + "Puede recuperar su etiqueta seleccionándolo en la tabla y pulsando «Generar Etiqueta QR».");
            }
            Alertas.error(e);
        }
    }
}
