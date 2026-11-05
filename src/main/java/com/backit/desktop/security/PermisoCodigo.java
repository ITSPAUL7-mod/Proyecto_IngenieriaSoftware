package com.backit.desktop.security;

/**
 * Catálogo de permisos (tabla permiso). name() es permiso.codigo.
 * Se siembran en el arranque (ver RbacSeedService) y se asignan por rol en {@link RbacCatalogo}.
 */
public enum PermisoCodigo {
    USUARIO_VER("Ver usuarios", "USUARIOS"),
    USUARIO_CREAR_PRIVILEGIADO("Crear SUPERADMIN, AUDITOR y ADMIN_SEDE", "USUARIOS"),
    USUARIO_CREAR_LOCAL("Crear PERSONAL_CUSTODIA y USUARIO_FINAL", "USUARIOS"),
    USUARIO_CAMBIAR_ESTADO("Activar, bloquear o inactivar cuentas", "USUARIOS"),

    INSTITUCION_GESTIONAR("Crear y editar instituciones", "INSTITUCIONES"),
    UBICACION_GESTIONAR("Gestionar sedes, zonas y puntos de entrega", "UBICACIONES"),
    CATEGORIA_GESTIONAR("Gestionar catálogo de categorías", "CATALOGOS"),

    OBJETO_VER("Consultar objetos encontrados", "OBJETOS"),
    OBJETO_REGISTRAR("Registrar hallazgos y generar QR", "OBJETOS"),

    REPORTE_PERDIDA_CREAR("Reportar objetos perdidos", "REPORTES_PERDIDA"),
    COINCIDENCIA_VER_PROPIAS("Ver coincidencias de los propios reportes", "COINCIDENCIAS"),
    COINCIDENCIA_REVISAR("Revisar coincidencias sugeridas", "COINCIDENCIAS"),

    RECLAMACION_CREAR("Presentar reclamaciones y evidencias", "RECLAMACIONES"),
    RECLAMACION_RESOLVER("Aprobar o rechazar reclamaciones", "RECLAMACIONES"),
    ENTREGA_REGISTRAR("Verificar identidad y registrar entrega física", "ENTREGAS"),
    VALORACION_CREAR("Valorar el servicio tras una entrega", "ENTREGAS"),

    AUDITORIA_VER("Consultar auditoría e integridad", "AUDITORIA"),
    CONFIGURACION_GESTIONAR("Gestionar parámetros globales", "CONFIGURACION"),
    TERMINOS_GESTIONAR("Publicar versiones de Términos y Condiciones", "TERMINOS");

    private final String nombre;
    private final String modulo;

    PermisoCodigo(String nombre, String modulo) {
        this.nombre = nombre;
        this.modulo = modulo;
    }

    public String nombre() {
        return nombre;
    }

    public String modulo() {
        return modulo;
    }
}
