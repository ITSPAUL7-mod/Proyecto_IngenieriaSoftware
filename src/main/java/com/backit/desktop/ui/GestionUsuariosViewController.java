package com.backit.desktop.ui;

import com.backit.desktop.dto.CrearInstitucionDto;
import com.backit.desktop.dto.CrearUsuarioDto;
import com.backit.desktop.security.EstadoUsuario;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.RolNombre;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.services.InstitucionService;
import com.backit.desktop.services.UsuarioService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import org.springframework.stereotype.Component;

@Component
public class GestionUsuariosViewController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<RolNombre> cmbRol;
    @FXML private ComboBox<Institucion> cmbInstitucion;
    @FXML private ComboBox<EstadoUsuario> cmbEstado;
    @FXML private Button btnCambiarEstado;
    @FXML private HBox boxNuevaInstitucion;
    @FXML private TextField txtNuevaInstitucion;
    @FXML private TableView<Usuario> tvUsuarios;
    @FXML private TableColumn<Usuario, String> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colCorreo;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colInstitucion;
    @FXML private TableColumn<Usuario, String> colEstado;

    private final UsuarioService usuarioService;
    private final InstitucionService institucionService;
    private final SesionActual sesion;

    public GestionUsuariosViewController(UsuarioService usuarioService, InstitucionService institucionService,
                                         SesionActual sesion) {
        this.usuarioService = usuarioService;
        this.institucionService = institucionService;
        this.sesion = sesion;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre() + " " + c.getValue().getApellido()));
        colCorreo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCorreo()));
        colRol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRol().getNombre()));
        colInstitucion.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getInstitucion() == null ? "(global)" : c.getValue().getInstitucion().getNombre()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));

        cmbInstitucion.setConverter(new StringConverter<>() {
            @Override public String toString(Institucion i) { return i == null ? "" : i.getNombre(); }
            @Override public Institucion fromString(String s) { return null; }
        });
        cmbEstado.getItems().setAll(EstadoUsuario.values());

        // Los roles ofrecidos dependen de quién esté logueado; el servicio lo vuelve a validar.
        cmbRol.getItems().setAll(usuarioService.rolesCreables());
        cmbRol.valueProperty().addListener((obs, anterior, rol) -> ajustarInstitucion(rol));

        boxNuevaInstitucion.setVisible(sesion.tienePermiso(PermisoCodigo.INSTITUCION_GESTIONAR));
        boxNuevaInstitucion.setManaged(boxNuevaInstitucion.isVisible());
        btnCambiarEstado.setDisable(!sesion.tienePermiso(PermisoCodigo.USUARIO_CAMBIAR_ESTADO));
        cmbEstado.setDisable(btnCambiarEstado.isDisabled());

        recargarInstituciones();
        recargarTabla();
    }

    private void ajustarInstitucion(RolNombre rol) {
        boolean global = rol != null && rol.esGlobal();
        // ADMIN_SEDE solo puede crear dentro de su institución: queda preseleccionada y bloqueada.
        boolean fija = sesion.rolActual() == RolNombre.ADMIN_SEDE;
        cmbInstitucion.setDisable(global || fija);
        if (global) {
            cmbInstitucion.getSelectionModel().clearSelection();
        } else if (fija && !cmbInstitucion.getItems().isEmpty()) {
            cmbInstitucion.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleGuardarUsuario() {
        try {
            RolNombre rol = cmbRol.getValue();
            Institucion inst = cmbInstitucion.getValue();
            boolean sinInstitucion = (rol != null && rol.esGlobal()) || inst == null;
            Long institucionId = sinInstitucion ? null : inst.getId();
            usuarioService.crear(new CrearUsuarioDto(
                    txtNombre.getText(), txtApellido.getText(), txtCorreo.getText(), txtPassword.getText(),
                    rol, institucionId));
            limpiarFormulario();
            recargarTabla();
            Alertas.info("Cuenta creada correctamente.");
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    @FXML
    private void handleCambiarEstado() {
        try {
            Usuario seleccionado = tvUsuarios.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                Alertas.info("Seleccione un usuario de la tabla.");
                return;
            }
            usuarioService.cambiarEstado(seleccionado.getId(), cmbEstado.getValue());
            recargarTabla();
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    @FXML
    private void handleCrearInstitucion() {
        try {
            institucionService.crear(new CrearInstitucionDto(txtNuevaInstitucion.getText(), null, null, null, null));
            txtNuevaInstitucion.clear();
            recargarInstituciones();
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    private void recargarTabla() {
        try {
            tvUsuarios.getItems().setAll(usuarioService.listarVisibles());
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    private void recargarInstituciones() {
        cmbInstitucion.getItems().setAll(institucionService.visibles());
        ajustarInstitucion(cmbRol.getValue());
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtApellido.clear();
        txtCorreo.clear();
        txtPassword.clear();
        cmbRol.getSelectionModel().clearSelection();
    }
}
