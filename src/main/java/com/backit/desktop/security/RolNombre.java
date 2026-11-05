package com.backit.desktop.security;

import java.util.List;

/**
 * Los cinco perfiles definidos en el Informe Técnico (sección 5).
 * El nombre del enum coincide con rol.nombre en la base de datos.
 *
 * nivelAcceso solo se usa para poblar rol.nivel_acceso; la decisión de "quién puede
 * crear a quién" NO se deriva del nivel sino de la matriz explícita de {@link #rolesCreables()},
 * porque AUDITOR no es un "escalón" entre ADMIN_SEDE y PERSONAL_CUSTODIA.
 */
public enum RolNombre {
    SUPERADMIN(100, "Administración técnica total, parámetros globales, usuarios privilegiados y recuperación controlada."),
    AUDITOR(60, "Consulta de auditoría, integridad, eventos y reportes; sin modificación de operación."),
    ADMIN_SEDE(80, "Gestión institucional, personal local, catálogos, reclamaciones y reportes."),
    PERSONAL_CUSTODIA(40, "Registro de hallazgos, QR, custodia, verificación y entrega física."),
    USUARIO_FINAL(10, "Reporte de pérdida, seguimiento de coincidencias, reclamaciones, evidencias y valoración.");

    private final short nivelAcceso;
    private final String descripcion;

    RolNombre(int nivelAcceso, String descripcion) {
        this.nivelAcceso = (short) nivelAcceso;
        this.descripcion = descripcion;
    }

    public short nivelAcceso() {
        return nivelAcceso;
    }

    public String descripcion() {
        return descripcion;
    }

    /** Cuentas globales: usuario.institucion_id = NULL (ver diseño de BD, tabla usuario). */
    public boolean esGlobal() {
        return this == SUPERADMIN || this == AUDITOR;
    }

    /** Cuentas que solo puede crear el SUPERADMIN. */
    public boolean esPrivilegiado() {
        return this == SUPERADMIN || this == AUDITOR || this == ADMIN_SEDE;
    }

    /** Matriz de aprovisionamiento: qué roles puede dar de alta este rol. */
    public List<RolNombre> rolesCreables() {
        return switch (this) {
            case SUPERADMIN -> List.of(ADMIN_SEDE, PERSONAL_CUSTODIA, USUARIO_FINAL, AUDITOR, SUPERADMIN);
            case ADMIN_SEDE -> List.of(PERSONAL_CUSTODIA, USUARIO_FINAL);
            default -> List.of();
        };
    }

    public boolean puedeCrear(RolNombre destino) {
        return rolesCreables().contains(destino);
    }

    public static RolNombre desde(String nombre) {
        try {
            return RolNombre.valueOf(nombre);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalStateException("Rol desconocido en la base de datos: " + nombre, e);
        }
    }
}
