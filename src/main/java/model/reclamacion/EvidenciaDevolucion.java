package model.reclamacion;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "evidencia_devolucion")
public class EvidenciaDevolucion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "reclamacion_id", nullable = false) private Reclamacion reclamacion;
    @Column(name = "tipo_evidencia", length = 50, nullable = false) private String tipoEvidencia;
    @Column(name = "ruta_archivo", columnDefinition = "TEXT", nullable = false) private String rutaArchivo;
    @Column(name = "hash_archivo", length = 64, nullable = false) private String hashArchivo;
    @Column(length = 300) private String descripcion;

    @ManyToOne @JoinColumn(name = "subido_por_id", nullable = false) private Usuario subidoPor;
    @Column(length = 20) private String clasificacion = "SENSIBLE";
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}