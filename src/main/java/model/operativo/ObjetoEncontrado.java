package model.operativo;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "objeto_encontrado")
public class ObjetoEncontrado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "institucion_id", nullable = false) private Institucion institucion;
    @ManyToOne @JoinColumn(name = "categoria_id", nullable = false) private CategoriaObjeto categoria;
    @ManyToOne @JoinColumn(name = "ubicacion_id", nullable = false) private Ubicacion ubicacion;
    @ManyToOne @JoinColumn(name = "encontrado_por_id", nullable = false) private Usuario encontradoPor;

    @Column(length = 150, nullable = false) private String titulo;
    @Column(columnDefinition = "TEXT", nullable = false) private String descripcion;
    @Column(name = "caracteristicas_privadas", columnDefinition = "TEXT") private String caracteristicasPrivadas;
    @Column(name = "fecha_encontrado", nullable = false) private OffsetDateTime fechaEncontrado;
    @Column(length = 30, nullable = false) private String estado;

    @Column(name = "codigo_qr", unique = true, nullable = false) private UUID codigoQr;
    @Column(name = "foto_ruta", columnDefinition = "TEXT") private String fotoRuta;
    @Column(name = "foto_hash", length = 64) private String fotoHash;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}