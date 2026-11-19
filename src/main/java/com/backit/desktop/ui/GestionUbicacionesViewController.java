package com.backit.desktop.ui;

import com.backit.desktop.dto.CrearUbicacionDto;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.services.InstitucionService;
import com.backit.desktop.services.UbicacionService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import model.operativo.Ubicacion;
import model.seguridad.Institucion;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Sprint 2 - pantalla real de ubicaciones (antes solo datos de ejemplo). */
@Component
public class GestionUbicacionesViewController {

    @FXML private TextField txtNombreUbicacion;
    @FXML private ComboBox<String> cmbTipoUbicacion;
    @FXML private ComboBox<Ubicacion> cmbUbicacionPadre;
    @FXML private TextField txtHorarioAtencion;
    @FXML private TreeView<String> tvJerarquiaUbicaciones;
    @FXML private ComboBox<Institucion> cmbInstitucion;

    private final UbicacionService ubicacionService;
    private final InstitucionService institucionService;
    private final SesionActual sesion;

    public GestionUbicacionesViewController(UbicacionService ubicacionService,
                                            InstitucionService institucionService,
                                            SesionActual sesion) {
        this.ubicacionService = ubicacionService;
        this.institucionService = institucionService;
        this.sesion = sesion;
    }

    @FXML
    public void initialize() {
        cmbTipoUbicacion.getItems().addAll("SEDE", "EDIFICIO", "PISO", "ZONA", "BODEGA");
        cmbUbicacionPadre.setConverter(new StringConverter<>() {
            @Override
            public String toString(Ubicacion u) {
                return u == null ? "" : u.getNombre() + " (" + u.getTipo() + ")";
            }

            @Override
            public Ubicacion fromString(String s) {
                return null;
            }
        });
        cmbInstitucion.setConverter(new StringConverter<>() {
            @Override
            public String toString(Institucion i) {
                return i == null ? "" : i.getNombre();
            }

            @Override
            public Institucion fromString(String s) {
                return null;
            }
        });

        try {
            boolean global = sesion.usuario().map(u -> u.getInstitucion() == null).orElse(true);
            cmbInstitucion.getItems().setAll(institucionService.visibles());
            if (global) {
                cmbInstitucion.setPromptText("Seleccione la institución");
            } else {
                cmbInstitucion.getSelectionModel().selectFirst();
                // Las cuentas locales siempre trabajan en su propia institución.
                cmbInstitucion.setVisible(false);
                cmbInstitucion.setManaged(false);
            }
            cmbInstitucion.getSelectionModel().selectedItemProperty().addListener(
                    (obs, antes, ahora) -> {
                        cmbUbicacionPadre.getSelectionModel().clearSelection();
                        recargar();
                    });
            recargar();
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    private void recargar() {
        try {
            Long institucionId = cmbInstitucion.getValue() == null ? null : cmbInstitucion.getValue().getId();
            List<Ubicacion> ubicaciones = ubicacionService.visiblesDe(institucionId);
            cmbUbicacionPadre.getItems().setAll(ubicaciones);
            construirArbol(ubicaciones);
        } catch (Exception e) {
            Alertas.error(e);
        }
    }

    private void construirArbol(List<Ubicacion> ubicaciones) {
        Map<Long, List<Ubicacion>> porPadre = ubicaciones.stream()
                .filter(u -> u.getUbicacionPadre() != null)
                .collect(Collectors.groupingBy(u -> u.getUbicacionPadre().getId()));

        TreeItem<String> raiz = new TreeItem<>("Ubicaciones de la institución");
        raiz.setExpanded(true);
        for (Ubicacion u : ubicaciones) {
            if (u.getUbicacionPadre() == null) {
                raiz.getChildren().add(nodo(u, porPadre));
            }
        }
        List<Long> ids = ubicaciones.stream().map(Ubicacion::getId).toList();
        for (Ubicacion u : ubicaciones) {
            Long padreId = u.getUbicacionPadre() == null ? null : u.getUbicacionPadre().getId();
            if (padreId != null && !ids.contains(padreId)) {
                raiz.getChildren().add(nodo(u, porPadre));
            }
        }
        tvJerarquiaUbicaciones.setRoot(raiz);
    }

    private TreeItem<String> nodo(Ubicacion u, Map<Long, List<Ubicacion>> porPadre) {
        TreeItem<String> item = new TreeItem<>(u.getTipo() + " · " + u.getNombre());
        item.setExpanded(true);
        for (Ubicacion hijo : porPadre.getOrDefault(u.getId(), new ArrayList<>())) {
            item.getChildren().add(nodo(hijo, porPadre));
        }
        return item;
    }

    @FXML
    private void handleGuardarUbicacion() {
        try {
            ubicacionService.crear(new CrearUbicacionDto(
                    txtNombreUbicacion.getText(),
                    cmbTipoUbicacion.getValue(),
                    null,
                    null,
                    cmbUbicacionPadre.getValue() == null ? null : cmbUbicacionPadre.getValue().getId(),
                    txtHorarioAtencion.getText(),
                    cmbInstitucion.getValue() == null ? null : cmbInstitucion.getValue().getId()));
            Alertas.info("Ubicación guardada correctamente.");
            txtNombreUbicacion.clear();
            txtHorarioAtencion.clear();
            recargar();
        } catch (Exception e) {
            Alertas.error(e);
        }
    }
}
