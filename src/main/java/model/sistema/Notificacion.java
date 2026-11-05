package model.sistema;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "notificacion")
public class Notificacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "usuario_id", nullable = false) private Usuario usuario;
    @Column(length = 150, nullable = false) private String titulo;
    @Column(columnDefinition = "TEXT", nullable = false) private String mensaje;
    @Column(length = 40, nullable = false) private String tipo;
    @Column(columnDefinition = "boolean default false") private Boolean leida = false;

    @Column(name = "fecha_envio") private OffsetDateTime fechaEnvio;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}