package model.operativo;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Institucion;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "ubicacion")
public class Ubicacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "institucion_id", nullable = false) private Institucion institucion;
    @ManyToOne @JoinColumn(name = "ubicacion_padre_id") private Ubicacion ubicacionPadre;

    @Column(length = 120, nullable = false) private String nombre;
    @Column(length = 30, nullable = false) private String tipo;
    @Column(length = 250) private String direccion;
    @Column(length = 250) private String referencia;
    @Column(columnDefinition = "boolean default true") private Boolean activo = true;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}