package com.backit.desktop.ui;

import com.backit.desktop.dto.ConfiguracionInicialDto;
import com.backit.desktop.services.ConfiguracionInicialService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

/** Asistente de primer arranque: crea la primera institución y el SUPERADMIN. */
@Component
public class SetupInicialViewController {

    @FXML private TextField txtInstitucion;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPasswordConfirmar;
    @FXML private Button btnCrear;

    private final ConfiguracionInicialService configuracion;
    private final Navegador navegador;

    public SetupInicialViewController(ConfiguracionInicialService configuracion, Navegador navegador) {
        this.configuracion = configuracion;
        this.navegador = navegador;
    }

    @FXML
    private void handleCrear() {
        try {
            configuracion.crearSuperAdminInicial(new ConfiguracionInicialDto(
                    txtInstitucion.getText(), txtNombre.getText(), txtApellido.getText(), txtCorreo.getText(),
                    txtPassword.getText(), txtPasswordConfirmar.getText()));
            txtPassword.clear();
            txtPasswordConfirmar.clear();
            Alertas.info("Configuración completada. Inicie sesión con la cuenta SUPERADMIN creada.");
            navegador.irA((Stage) btnCrear.getScene().getWindow(), "LoginView.fxml", 1050, 650, "BackIt - Acceso");
        } catch (Exception e) {
            Alertas.error(e);
        }
    }
}
