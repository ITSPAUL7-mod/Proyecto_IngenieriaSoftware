package model.seguridad;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "permiso")
public class Permiso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false) private String nombre;
    @Column(length = 100, unique = true, nullable = false) private String codigo;
    @Column(length = 60, nullable = false) private String modulo;
    @Column(length = 250) private String descripcion;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
    @Column(name = "actualizado_en") private OffsetDateTime actualizadoEn;
}