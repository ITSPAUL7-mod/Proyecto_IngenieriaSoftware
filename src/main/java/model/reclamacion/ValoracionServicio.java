package model.reclamacion;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "valoracion_servicio")
public class ValoracionServicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne @JoinColumn(name = "reclamacion_id", unique = true, nullable = false) private Reclamacion reclamacion;
    @ManyToOne @JoinColumn(name = "usuario_id", nullable = false) private Usuario usuario;

    private Short calificacion;
    @Column(length = 500) private String comentario;
    @Column(name = "fecha_valoracion") private OffsetDateTime fechaValoracion;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}