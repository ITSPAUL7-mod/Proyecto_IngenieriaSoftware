package com.backit.desktop.services;

import com.backit.desktop.repositories.PermisoRepository;
import com.backit.desktop.repositories.RolPermisoRepository;
import com.backit.desktop.repositories.RolRepository;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.RbacCatalogo;
import com.backit.desktop.security.RolNombre;
import model.seguridad.Permiso;
import model.seguridad.Rol;
import model.seguridad.RolPermiso;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Siembra roles, permisos y rol_permiso al arrancar. Es idempotente y solo AÑADE lo que falta:
 * no pisa cambios hechos después por un SUPERADMIN.
 * Debe correr antes de mostrar cualquier pantalla (Spring ejecuta los ApplicationRunner dentro de SpringApplication.run).
 */
@Service
public class RbacSeedService implements ApplicationRunner {

    private final RolRepository roles;
    private final PermisoRepository permisos;
    private final RolPermisoRepository rolPermisos;

    public RbacSeedService(RolRepository roles, PermisoRepository permisos, RolPermisoRepository rolPermisos) {
        this.roles = roles;
        this.permisos = permisos;
        this.rolPermisos = rolPermisos;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        sincronizar();
    }

    @Transactional
    public void sincronizar() {
        OffsetDateTime ahora = OffsetDateTime.now();

        for (PermisoCodigo codigo : PermisoCodigo.values()) {
            if (permisos.findByCodigo(codigo.name()).isEmpty()) {
                Permiso p = new Permiso();
                p.setCodigo(codigo.name());
                p.setNombre(codigo.nombre());
                p.setModulo(codigo.modulo());
                p.setDescripcion(codigo.nombre());
                p.setCreadoEn(ahora);
                p.setActualizadoEn(ahora);
                permisos.save(p);
            }
        }

        for (RolNombre nombre : RolNombre.values()) {
            Rol rol = roles.findByNombre(nombre.name()).orElseGet(() -> {
                Rol r = new Rol();
                r.setNombre(nombre.name());
                r.setDescripcion(nombre.descripcion());
                r.setNivelAcceso(nombre.nivelAcceso());
                r.setActivo(true);
                r.setCreadoEn(ahora);
                r.setActualizadoEn(ahora);
                return roles.save(r);
            });

            for (PermisoCodigo codigo : RbacCatalogo.permisosDe(nombre)) {
                Permiso permiso = permisos.findByCodigo(codigo.name()).orElseThrow();
                if (!rolPermisos.existsByRolIdAndPermisoId(rol.getId(), permiso.getId())) {
                    RolPermiso rp = new RolPermiso();
                    rp.setRol(rol);
                    rp.setPermiso(permiso);
                    rp.setActivo(true);
                    rp.setCreadoEn(ahora);
                    rp.setActualizadoEn(ahora);
                    rolPermisos.save(rp);
                }
            }
        }
    }
}
