package com.backit.desktop.repositories;

import model.seguridad.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {

    boolean existsByRolIdAndPermisoId(Long rolId, Long permisoId);

    @Query("select rp.permiso.codigo from RolPermiso rp where rp.rol.id = :rolId and rp.activo = true")
    List<String> findCodigosActivosByRolId(@Param("rolId") Long rolId);
}
