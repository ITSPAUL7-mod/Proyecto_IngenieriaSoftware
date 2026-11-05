package com.backit.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.backit.desktop.services.ConfiguracionInicialService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

// UserDetailsServiceAutoConfiguration crearía un usuario "user" con contraseña aleatoria en el log:
// BackIt autentica contra la tabla usuario (AuthService), así que se excluye.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class Main extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = SpringApplication.run(Main.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Primer arranque (sin SUPERADMIN en la BD) → asistente; en los demás casos → login.
        boolean primerArranque = springContext.getBean(ConfiguracionInicialService.class).requiereConfiguracionInicial();
        String vista = primerArranque ? "/templates/SetupInicialView.fxml" : "/templates/LoginView.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(vista));

        loader.setControllerFactory(springContext::getBean);

        Parent root = loader.load();

        primaryStage.setTitle(primerArranque ? "BackIt - Configuración inicial" : "BackIt - Acceso");
        primaryStage.setScene(new Scene(root, 1050, 650));
        primaryStage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }
}