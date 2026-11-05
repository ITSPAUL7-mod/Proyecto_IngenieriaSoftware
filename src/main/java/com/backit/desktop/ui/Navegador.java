package com.backit.desktop.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Carga FXML con controladores gestionados por Spring y cambia la escena de la ventana. */
@Component
public class Navegador {

    private final ConfigurableApplicationContext springContext;

    public Navegador(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    public Parent cargar(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/" + fxml));
        loader.setControllerFactory(springContext::getBean);
        return loader.load();
    }

    public void irA(Stage stage, String fxml, double ancho, double alto, String titulo) throws IOException {
        stage.setScene(new Scene(cargar(fxml), ancho, alto));
        stage.setTitle(titulo);
        stage.centerOnScreen();
    }
}
