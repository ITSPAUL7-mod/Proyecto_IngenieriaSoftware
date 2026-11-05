package com.backit.desktop.security;

import com.backit.desktop.exceptions.AccesoDenegadoException;
import org.springframework.stereotype.Service;

/**
 * RBAC en la capa de servicios. Ocultar un botón en la UI NO sustituye esta comprobación
 * (Informe Técnico, sección 10, control "Perfiles").
 */
@Service
public class AutorizacionService {

    private final SesionActual sesion;

    public AutorizacionService(SesionActual sesion) {
        this.sesion = sesion;
    }

    public void requerir(PermisoCodigo permiso) {
        sesion.requerirUsuario();
        if (!sesion.tienePermiso(permiso)) {
            throw new AccesoDenegadoException("No tiene permiso para realizar esta operación.");
        }
    }
}
