package model.seguridad;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "rol_permiso")
public class RolPermiso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "rol_id", nullable = false) private Rol rol;
    @ManyToOne @JoinColumn(name = "permiso_id", nullable = false) private Permiso permiso;

    @Column(columnDefinition = "boolean default true") private Boolean activo = true;
    @ManyToOne @JoinColumn(name = "asignado_por") private Usuario asignadoPor;

    @Column(name = "creado_en") private OffsetDateTime creadoEn;
    @Column(name = "actualizado_en") private OffsetDateTime actualizadoEn;
}