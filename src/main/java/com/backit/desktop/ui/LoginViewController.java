package com.backit.desktop.ui;

import com.backit.desktop.exceptions.AutenticacionException;
import com.backit.desktop.services.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class LoginViewController {

    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    private final AuthService authService;
    private final Navegador navegador;

    public LoginViewController(AuthService authService, Navegador navegador) {
        this.authService = authService;
        this.navegador = navegador;
    }

    @FXML
    private void handleLogin() {
        lblError.setVisible(false);
        try {
            authService.autenticar(txtCorreo.getText(), txtPassword.getText());
            txtPassword.clear();
            // TODO Sprint 1: antes de entrar, verificar la aceptación de la versión vigente de Términos y Condiciones.
            navegador.irA((Stage) btnLogin.getScene().getWindow(), "MainLayout.fxml", 1280, 720,
                    "BackIt | Sistema Institucional de Objetos Perdidos");
        } catch (AutenticacionException e) {
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
        } catch (Exception e) {
            Alertas.error(e);
        }
    }
}
