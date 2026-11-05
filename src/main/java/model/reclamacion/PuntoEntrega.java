package model.reclamacion;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import model.operativo.Ubicacion;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "punto_entrega")
public class PuntoEntrega {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "institucion_id", nullable = false) private Institucion institucion;
    @ManyToOne @JoinColumn(name = "ubicacion_id", nullable = false) private Ubicacion ubicacion;

    @Column(name = "nombre_punto", length = 120, nullable = false) private String nombrePunto;
    @ManyToOne @JoinColumn(name = "encargado_id") private Usuario encargado;
    @Column(name = "horario_atencion", length = 150) private String horarioAtencion;

    @Column(columnDefinition = "boolean default true") private Boolean activo = true;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}