package com.backit.desktop.prototipo;

import javafx.application.Application;
import javafx.stage.Stage;

public class PrototipoLauncher extends Application {
    public static void main(String[] args) { launch(args); }

    @Override public void start(Stage stage) throws Exception {
        PantallasBosquejo.mostrar(stage, "LoginView.fxml", "BackIt - Prototipo inicial");
    }
}
