package com.backit.desktop.ui;

import com.backit.desktop.exceptions.AutenticacionException;
import com.backit.desktop.exceptions.BackItException;
import com.backit.desktop.services.AuthService;
import com.backit.desktop.services.RecuperacionService;
import com.backit.desktop.services.TerminoCondicionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.seguridad.Usuario;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LoginViewController {

    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;
    @FXML private Hyperlink lnkRecuperar;

    private final AuthService authService;
    private final RecuperacionService recuperacionService;
    private final TerminoCondicionService terminosService;
    private final Navegador navegador;

    public LoginViewController(AuthService authService, RecuperacionService recuperacionService,
                               TerminoCondicionService terminosService, Navegador navegador) {
        this.authService = authService;
        this.recuperacionService = recuperacionService;
        this.terminosService = terminosService;
        this.navegador = navegador;
    }

    @FXML
    private void handleLogin() {
        lblError.setVisible(false);
        try {
            Usuario usuario = authService.autenticar(txtCorreo.getText(), txtPassword.getText());
            txtPassword.clear();
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            if (terminosService.requiereAceptacion(usuario)) {
                mostrarTerminos(stage);
            } else {
                abrirPrincipal(stage);
            }
        } catch (AutenticacionException e) {
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    /** Ventana modal bloqueante: no se puede entrar al menu sin una decision. */
    private void mostrarTerminos(Stage owner) throws Exception {
        FXMLLoader loader = navegador.crearLoader("TerminosModal.fxml");
        Parent vista = loader.load();
        TerminosModalController controlador = loader.getController();

        Stage modal = new Stage();
        modal.initOwner(owner);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initStyle(javafx.stage.StageStyle.DECORATED);
        modal.setTitle("BackIt | Términos y Condiciones");
        modal.setScene(new Scene(vista, 650, 500));
        modal.setResizable(false);

        controlador.alAceptar(() -> {
            modal.close();
            try {
                abrirPrincipal(owner);
            } catch (Exception e) {
                Alertas.error(e);
            }
        });
        controlador.alRechazar(() -> {
            authService.cerrarSesion();
            modal.close();
            owner.close();
        });
        modal.setOnCloseRequest(e -> {
            e.consume();
            controlador.rechazar();
        });
        modal.show();
    }

    private void abrirPrincipal(Stage stage) throws Exception {
        navegador.irA(stage, "MainLayout.fxml", 1280, 720,
                "BackIt | Sistema Institucional de Objetos Perdidos");
    }

    /** Sprint 1 - flujo de recuperacion de cuenta (sin correo: se muestra el token en pantalla). */
    @FXML
    private void handleRecuperar() {
        try {
            TextInputDialog correoDialog = new TextInputDialog();
            correoDialog.setTitle("BackIt");
            correoDialog.setHeaderText("Recuperación de cuenta");
            correoDialog.setContentText("Ingrese su correo institucional:");
            Optional<String> correo = correoDialog.showAndWait();
            if (correo.isEmpty() || correo.get().isBlank()) {
                return;
            }

            String token = recuperacionService.solicitar(correo.get());

            // Un Alert no permite seleccionar texto: se usa un Dialog con un TextField
            // y un boton que copia el codigo al portapapeles del sistema.
            Dialog<ButtonType> aviso = new Dialog<>();
            aviso.setTitle("BackIt");
            aviso.setHeaderText("Código de recuperación");
            TextField txtCodigo = new TextField(token);
            txtCodigo.setEditable(false);
            txtCodigo.setPrefWidth(300);
            Button btnCopiar = new Button("Copiar");
            btnCopiar.setOnAction(e -> {
                ClipboardContent contenidoPortapapeles = new ClipboardContent();
                contenidoPortapapeles.putString(token);
                Clipboard.getSystemClipboard().setContent(contenidoPortapapeles);
                txtCodigo.selectAll();
                btnCopiar.setText("Copiado");
            });
            HBox filaCodigo = new HBox(8, txtCodigo, btnCopiar);
            filaCodigo.setAlignment(Pos.CENTER_LEFT);
            VBox contenidoAviso = new VBox(10,
                    new Label("Cópielo y péguelo en el siguiente paso. Válido 30 minutos."),
                    filaCodigo);
            contenidoAviso.setPadding(new Insets(15));
            aviso.getDialogPane().setContent(contenidoAviso);
            aviso.getDialogPane().getButtonTypes().add(ButtonType.OK);
            aviso.showAndWait();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("BackIt");
            dialog.setHeaderText("Restablecer contraseña");
            TextField txtToken = new TextField();
            txtToken.setPromptText("Código recibido");
            PasswordField txtNueva = new PasswordField();
            txtNueva.setPromptText("Nueva contraseña");
            GridPane contenido = new GridPane();
            contenido.setHgap(10);
            contenido.setVgap(10);
            contenido.setPadding(new Insets(15));
            contenido.add(new Label("Código:"), 0, 0);
            contenido.add(txtToken, 1, 0);
            contenido.add(new Label("Nueva clave:"), 0, 1);
            contenido.add(txtNueva, 1, 1);
            dialog.getDialogPane().setContent(contenido);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Optional<ButtonType> respuesta = dialog.showAndWait();
            if (respuesta.filter(ButtonType.OK::equals).isPresent()) {
                recuperacionService.restablecer(txtToken.getText(), txtNueva.getText());
                Alertas.info("Contraseña actualizada. Inicie sesión con la nueva contraseña.");
            }
        } catch (BackItException e) {
            Alertas.error(e);
        } catch (Exception e) {
            Alertas.error(e);
        }
    }
}
