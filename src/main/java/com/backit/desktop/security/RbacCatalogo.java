package com.backit.desktop.security;

import java.util.EnumSet;
import java.util.Set;

import static com.backit.desktop.security.PermisoCodigo.*;

/** Matriz rol → permisos que se siembra en rol_permiso. Fuente única de verdad del RBAC inicial. */
public final class RbacCatalogo {

    private RbacCatalogo() {
    }

    public static Set<PermisoCodigo> permisosDe(RolNombre rol) {
        return switch (rol) {
            case SUPERADMIN -> EnumSet.allOf(PermisoCodigo.class);

            case AUDITOR -> EnumSet.of(AUDITORIA_VER, USUARIO_VER);

            case ADMIN_SEDE -> EnumSet.of(
                    USUARIO_VER, USUARIO_CREAR_LOCAL, USUARIO_CAMBIAR_ESTADO,
                    UBICACION_GESTIONAR, CATEGORIA_GESTIONAR,
                    OBJETO_VER, COINCIDENCIA_REVISAR, RECLAMACION_RESOLVER);

            case PERSONAL_CUSTODIA -> EnumSet.of(
                    OBJETO_VER, OBJETO_REGISTRAR, COINCIDENCIA_REVISAR, ENTREGA_REGISTRAR);

            case USUARIO_FINAL -> EnumSet.of(
                    REPORTE_PERDIDA_CREAR, COINCIDENCIA_VER_PROPIAS, RECLAMACION_CREAR, VALORACION_CREAR);
        };
    }
}
