package com.backit.desktop.prototipo;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ModuloBosquejoController {
    @FXML private ComboBox<String> cmbCategoria, cmbUbicacion, cmbCategoriaPerdida,
            cmbUbicacionPerdida, cmbFiltroCategoria, cmbTipoEvidencia, cmbTipoUbicacion,
            cmbUbicacionPadre, cmbRol, cmbInstitucion, cmbEstado;
    @FXML private TableView<ObservableList<String>> tvObjetos, tvCatalogo, tvCoincidencias,
            tvReclamaciones, tvUsuarios, tvAuditoria, tvErrores;
    @FXML private TableView<ObservableList<String>> tvGenerico;
    @FXML private TreeView<String> tvJerarquiaUbicaciones;
    @FXML private ProgressBar pbScoreTotal;
    @FXML private Label lblScoreTotal, lblCat, lblUbic, lblFecha, lblDesc, lblCaract,
            lblFotoInfo, lblArchivoInfo, lblObjetoInfo, lblReclamanteInfo;
    @FXML private Button btnConfirmarEntrega;
    @FXML private TextField txtCodigoQR, txtTitulo, txtNombre, txtCorreo, txtNombreUbicacion;

    @FXML public void initialize() {
        llenarCombo(cmbCategoria, "Electronica", "Documentos", "Prendas", "Accesorios", "Otros");
        llenarCombo(cmbUbicacion, "Biblioteca", "Edificio A", "Cafeteria");
        llenarCombo(cmbCategoriaPerdida, "Electronica", "Documentos", "Prendas", "Accesorios");
        llenarCombo(cmbUbicacionPerdida, "Biblioteca", "Edificio A", "Cafeteria");
        llenarCombo(cmbFiltroCategoria, "Todos", "Electronica", "Documentos", "Prendas");
        llenarCombo(cmbTipoEvidencia, "Factura", "Foto", "Senia privada", "Firma");
        llenarCombo(cmbTipoUbicacion, "SEDE", "EDIFICIO", "PISO", "ZONA", "BODEGA");
        llenarCombo(cmbUbicacionPadre, "Sede central", "Edificio A");
        llenarCombo(cmbRol, "SUPERADMIN", "AUDITOR", "ADMIN_SEDE", "PERSONAL_CUSTODIA", "USUARIO_FINAL");
        llenarCombo(cmbInstitucion, "Institucion ejemplo");
        llenarCombo(cmbEstado, "ACTIVO", "INACTIVO", "BLOQUEADO");
        if (tvJerarquiaUbicaciones != null) {
            TreeItem<String> base = new TreeItem<>("Institucion ejemplo (demo)");
            base.getChildren().add(new TreeItem<>("Sede principal / Bodega 1"));
            base.setExpanded(true);
            tvJerarquiaUbicaciones.setRoot(base);
        }
        if (pbScoreTotal != null) pbScoreTotal.setProgress(0.78);
        if (lblScoreTotal != null) lblScoreTotal.setText("78% (ejemplo, no calculado)");
        if (lblCat != null) lblCat.setText("Categoria: ejemplo");
        if (lblUbic != null) lblUbic.setText("Ubicacion: ejemplo");
        if (lblFecha != null) lblFecha.setText("Fecha: ejemplo");
        if (lblDesc != null) lblDesc.setText("Descripcion: ejemplo");
        if (lblCaract != null) lblCaract.setText("Caracteristicas: ocultas en demo");
        agregar(tvObjetos, "DEM-001", "Mochila negra", "Accesorios", "Biblioteca", "04/11/2026", "EN CUSTODIA");
        agregar(tvObjetos, "DEM-002", "Llaves", "Otros", "Cafeteria", "03/11/2026", "REGISTRADO");
        agregar(tvCatalogo, "DEM-001", "Mochila negra", "Accesorios", "Biblioteca", "04/11/2026", "DISPONIBLE");
        agregar(tvCoincidencias, "CO-01", "Mochila negra", "RE-01", "78% EJEMPLO", "SUGERIDA");
        agregar(tvReclamaciones, "RC-01", "Mochila negra", "Usuario ficticio", "PENDIENTE", "EJEMPLO");
        agregar(tvUsuarios, "U-01", "Andrea Demo", "demo@backit.edu", "ADMIN_SEDE", "Institucion ejemplo", "ACTIVO");
        agregar(tvAuditoria, "04/11/2026", "demo", "Vista de muestra", "Prototipo", "SIN HASH REAL");
        agregar(tvErrores, "DEM-0", "Bosquejo", "Sin log real todavia", "04/11/2026");
        agregar(tvGenerico, "Ejemplo", "Modulo futuro", "Pendiente de desarrollo");
    }

    private void llenarCombo(ComboBox<String> combo, String... valores) {
        if (combo != null) combo.getItems().setAll(valores);
    }

    @SuppressWarnings("unchecked")
    private void agregar(TableView<ObservableList<String>> tabla, String... celdas) {
        if (tabla == null) return;
        var columnas = tabla.getColumns();
        for (int i = 0; i < columnas.size(); i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c =
                    (TableColumn<ObservableList<String>, String>) (TableColumn<?, ?>) columnas.get(i);
            c.setCellValueFactory(x -> new SimpleStringProperty(
                idx < x.getValue().size() ? x.getValue().get(idx) : ""));
        }
        tabla.getItems().add(FXCollections.observableArrayList(celdas));
    }

    @FXML private void handleCargarFoto() {
        if (lblFotoInfo != null) lblFotoInfo.setText("foto_ejemplo.jpg (simulada)");
        PantallasBosquejo.aviso("Objetos encontrados", "Seleccion de fotografia (archivo ficticio)");
    }
    @FXML private void handleGuardar() {
        String titulo = txtTitulo == null || txtTitulo.getText().isBlank() ? "Objeto de ejemplo" : txtTitulo.getText();
        agregar(tvObjetos, "DEM-003", titulo, "Otros", "Sede principal", "04/11/2026", "DEMO");
        PantallasBosquejo.aviso("Objetos encontrados", "Registro simulado de objeto encontrado");
    }
    @FXML private void handleGenerarQR() {
        PantallasBosquejo.aviso("Objetos encontrados", "Generacion futura de etiqueta QR (sin token real)");
    }
    @FXML private void handleRegistrarPerdida() {
        PantallasBosquejo.aviso("Reporte de perdida", "Registro simulado de reporte de perdida");
    }
    @FXML private void handleBuscar() {
        PantallasBosquejo.aviso("Reporte de perdida", "Consulta visual de catalogo");
    }
    @FXML private void handleIniciarReclamacion() {
        PantallasBosquejo.aviso("Coincidencias", "Inicio simulado de reclamacion (sin registro real)");
    }
    @FXML private void handleAdjuntarArchivo() {
        if (lblArchivoInfo != null) lblArchivoInfo.setText("evidencia_ejemplo.pdf (simulada)");
        PantallasBosquejo.aviso("Reclamaciones", "Adjuntar evidencia de ejemplo");
    }
    @FXML private void handleGuardarEvidencia() {
        PantallasBosquejo.aviso("Reclamaciones", "Guardar evidencia (pendiente de implementacion)");
    }
    @FXML private void handleAprobar() {
        PantallasBosquejo.aviso("Reclamaciones", "Aprobacion simulada, sin validar identidad");
    }
    @FXML private void handleRechazar() {
        PantallasBosquejo.aviso("Reclamaciones", "Rechazo simulado, sin cambios persistentes");
    }
    @FXML private void handleValidarQR() {
        if (txtCodigoQR != null && txtCodigoQR.getText().isBlank()) {
            PantallasBosquejo.aviso("Entrega fisica", "Introduzca un codigo de ejemplo para explorar el flujo");
            return;
        }
        if (lblObjetoInfo != null) lblObjetoInfo.setText("Objeto EJEMPLO: mochila negra");
        if (lblReclamanteInfo != null) lblReclamanteInfo.setText("Reclamante EJEMPLO: Usuario ficticio");
        if (btnConfirmarEntrega != null) btnConfirmarEntrega.setDisable(false);
        PantallasBosquejo.aviso("Entrega fisica", "Validacion visual de QR, sin escaneo ni verificacion");
    }
    @FXML private void handleConfirmarEntrega() {
        PantallasBosquejo.aviso("Entrega fisica", "Entrega ficticia, sin modificar el inventario");
    }
    @FXML private void handleEnviarValoracion() {
        PantallasBosquejo.aviso("Entrega fisica", "Registro de valoracion simulado");
    }
    @FXML private void handleGuardarUsuario() {
        String nombre = txtNombre == null || txtNombre.getText().isBlank() ? "Usuario ficticio" : txtNombre.getText();
        String correo = txtCorreo == null || txtCorreo.getText().isBlank() ? "demo@backit.edu" : txtCorreo.getText();
        agregar(tvUsuarios, "DEMO", nombre, correo, "ADMIN_SEDE", "Institucion ejemplo", "SIMULADO");
        PantallasBosquejo.aviso("Usuarios y roles", "Alta simulada de cuenta y rol");
    }
    @FXML private void handleCambiarEstado() {
        PantallasBosquejo.aviso("Usuarios y roles", "Cambio de estado simulado");
    }
    @FXML private void handleCrearInstitucion() {
        PantallasBosquejo.aviso("Usuarios y roles", "Alta simulada de institucion");
    }
    @FXML private void handleGuardarUbicacion() {
        if (tvJerarquiaUbicaciones != null && tvJerarquiaUbicaciones.getRoot() != null)
            tvJerarquiaUbicaciones.getRoot().getChildren().add(new TreeItem<>(
                txtNombreUbicacion == null || txtNombreUbicacion.getText().isBlank() ? "Nueva ubicacion (demo)" : txtNombreUbicacion.getText()));
        PantallasBosquejo.aviso("Sedes y puntos", "Guardado simulado de ubicacion");
    }
    @FXML private void handleFiltrar() {
        PantallasBosquejo.aviso("Auditoria", "Consulta visual; auditoria autentica aun no implementada");
    }
    @FXML private void handleVerNotificaciones() {
        PantallasBosquejo.aviso("Notificaciones", "Visualizacion ficticia de notificaciones");
    }
    @FXML private void handleConsultarHistorial() {
        PantallasBosquejo.aviso("Historial", "Consulta de historial de estados no persistido");
    }
    @FXML private void handleGenerarReporte() {
        PantallasBosquejo.aviso("Reportes", "Generacion prevista de informe estadistico");
    }
    @FXML private void handleGuardarConfiguracion() {
        PantallasBosquejo.aviso("Configuracion", "Cambio de parametros no guardado");
    }
}
