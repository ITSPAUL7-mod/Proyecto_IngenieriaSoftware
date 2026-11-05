package com.backit.desktop.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.services.AuthService;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainLayoutController {

    @FXML
    private Label lblUsuarioInfo;
    @FXML
    private Label lblEstadoConexion;
    @FXML
    private Label lblHuellaDispositivo;
    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnNavObjetosEncontrados;
    @FXML
    private Button btnNavReportePerdida;
    @FXML
    private Button btnNavCoincidencias;
    @FXML
    private Button btnNavReclamaciones;
    @FXML
    private Button btnNavEntregaFisica;
    @FXML
    private Button btnNavGestionUsuarios;
    @FXML
    private Button btnNavGestionUbicaciones;
    @FXML
    private Button btnNavAuditoria;

    private final ConfigurableApplicationContext springContext;
    private final SesionActual sesion;
    private final AuthService authService;
    private final Navegador navegador;

    // Inyectamos el contexto de Spring para que funcione el setControllerFactory
    public MainLayoutController(ConfigurableApplicationContext springContext, SesionActual sesion,
                                AuthService authService, Navegador navegador) {
        this.springContext = springContext;
        this.sesion = sesion;
        this.authService = authService;
        this.navegador = navegador;
    }

    @FXML
    public void initialize() {
        lblEstadoConexion.setText("Estado: Base de Datos Local Conectada | RBAC Activo");
        sesion.usuario().ifPresent(u -> lblUsuarioInfo.setText(
                "Usuario: " + u.getNombre() + " " + u.getApellido() + " (" + u.getRol().getNombre() + ")"));

        // Solo comodidad visual: la autorización real se vuelve a comprobar en cada servicio.
        mostrarSi(btnNavObjetosEncontrados, PermisoCodigo.OBJETO_VER);
        mostrarSi(btnNavReportePerdida, PermisoCodigo.REPORTE_PERDIDA_CREAR);
        mostrarSi(btnNavCoincidencias, PermisoCodigo.COINCIDENCIA_REVISAR, PermisoCodigo.COINCIDENCIA_VER_PROPIAS);
        mostrarSi(btnNavReclamaciones, PermisoCodigo.RECLAMACION_CREAR, PermisoCodigo.RECLAMACION_RESOLVER);
        mostrarSi(btnNavEntregaFisica, PermisoCodigo.ENTREGA_REGISTRAR);
        mostrarSi(btnNavGestionUsuarios, PermisoCodigo.USUARIO_VER);
        mostrarSi(btnNavGestionUbicaciones, PermisoCodigo.UBICACION_GESTIONAR);
        mostrarSi(btnNavAuditoria, PermisoCodigo.AUDITORIA_VER);
    }

    private void mostrarSi(Node nodo, PermisoCodigo... alguno) {
        boolean visible = false;
        for (PermisoCodigo p : alguno) {
            visible |= sesion.tienePermiso(p);
        }
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }

    private void cargarVista(String fxmlFile) {
        try {
            // ruta de archivos vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/" + fxmlFile));

            // controladores de las vistas hijas usen Spring
            loader.setControllerFactory(springContext::getBean);

            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar el archivo FXML: " + fxmlFile);
        }
    }

    @FXML
    private void navObjetosEncontrados() {
        cargarVista("ObjetosEncontradosView.fxml");
    }

    @FXML
    private void navReportePerdida() {
        cargarVista("ReportePerdidaView.fxml");
    }

    @FXML
    private void navCoincidencias() {
        cargarVista("CoincidenciasView.fxml");
    }

    @FXML
    private void navReclamaciones() {
        cargarVista("ReclamacionesView.fxml");
    }

    @FXML
    private void navEntregaFisica() {
        cargarVista("EntregaFisicaView.fxml");
    }

    @FXML
    private void navGestionUsuarios() {
        cargarVista("GestionUsuariosView.fxml");
    }

    @FXML
    private void navGestionUbicaciones() {
        cargarVista("GestionUbicacionesView.fxml");
    }

    @FXML
    private void navAuditoria() {
        cargarVista("AuditoriaView.fxml");
    }

    @FXML
    private void handleCerrarSesion() {
        try {
            authService.cerrarSesion();
            navegador.irA((Stage) contentArea.getScene().getWindow(), "LoginView.fxml", 1050, 650, "BackIt - Acceso");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}