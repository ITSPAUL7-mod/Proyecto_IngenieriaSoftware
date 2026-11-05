package model.seguridad;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "rol")
public class Rol {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false) private String nombre;
    @Column(length = 250) private String descripcion;
    @Column(name = "nivel_acceso", nullable = false) private Short nivelAcceso;
    @Column(columnDefinition = "boolean default true") private Boolean activo = true;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
    @Column(name = "actualizado_en") private OffsetDateTime actualizadoEn;
}