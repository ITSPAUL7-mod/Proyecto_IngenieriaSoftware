package model.sistema;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "termino_condicion")
public class TerminoCondicion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, unique = true, nullable = false) private String version;
    @Column(length = 150, nullable = false) private String titulo;
    @Column(columnDefinition = "TEXT", nullable = false) private String contenido;
    @Column(name = "contenido_hash", length = 64, nullable = false) private String contenidoHash;
    @Column(name = "fecha_vigencia", nullable = false) private OffsetDateTime fechaVigencia;
    @Column(columnDefinition = "boolean default true") private Boolean activo = true;

    @ManyToOne @JoinColumn(name = "creado_por_id") private Usuario creadoPor;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}