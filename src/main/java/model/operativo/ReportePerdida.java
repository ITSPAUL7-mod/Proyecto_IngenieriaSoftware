package model.operativo;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "reporte_perdida")
public class ReportePerdida {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "institucion_id", nullable = false) private Institucion institucion;
    @ManyToOne @JoinColumn(name = "usuario_id", nullable = false) private Usuario usuario;
    @ManyToOne @JoinColumn(name = "categoria_id", nullable = false) private CategoriaObjeto categoria;
    @ManyToOne @JoinColumn(name = "ubicacion_perdida_id") private Ubicacion ubicacionPerdida;

    @Column(length = 150, nullable = false) private String titulo;
    @Column(columnDefinition = "TEXT", nullable = false) private String descripcion;
    @Column(name = "caracteristicas_privadas", columnDefinition = "TEXT") private String caracteristicasPrivadas;
    @Column(name = "fecha_perdida", nullable = false) private OffsetDateTime fechaPerdida;
    @Column(length = 30, nullable = false) private String estado;

    @Column(name = "creado_en") private OffsetDateTime creadoEn;
    @Column(name = "actualizado_en") private OffsetDateTime actualizadoEn;
}