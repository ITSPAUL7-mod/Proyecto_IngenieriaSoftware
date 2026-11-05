package model.operativo;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "categoria_objeto")
public class CategoriaObjeto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, unique = true, nullable = false) private String nombre;
    @Column(length = 250) private String descripcion;
    @Column(length = 120) private String icono;
    @Column(columnDefinition = "boolean default true") private Boolean activo = true;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
    @Column(name = "actualizado_en") private OffsetDateTime actualizadoEn;
}