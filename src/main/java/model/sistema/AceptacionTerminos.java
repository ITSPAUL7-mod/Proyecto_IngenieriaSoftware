package model.sistema;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "aceptacion_terminos")
public class AceptacionTerminos {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "usuario_id", nullable = false) private Usuario usuario;
    @ManyToOne @JoinColumn(name = "termino_id", nullable = false) private TerminoCondicion terminoCondicion;

    @Column(name = "fecha_aceptacion") private OffsetDateTime fechaAceptacion;
    @Column(name = "dispositivo_hash", length = 128) private String dispositivoHash;
    @Column(length = 120) private String origen;
    @Column(nullable = false) private Boolean aceptado;
    @Column(name = "revocado_en") private OffsetDateTime revocadoEn;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}