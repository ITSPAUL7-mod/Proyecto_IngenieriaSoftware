package model.sistema;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "error_sistema")
public class ErrorSistema {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_error", length = 60, nullable = false) private String codigoError;
    @Column(name = "mensaje_error", columnDefinition = "TEXT", nullable = false) private String mensajeError;
    @Column(name = "traza_pila", columnDefinition = "TEXT") private String trazaPila;
    @Column(length = 80, nullable = false) private String modulo;
    @Column(length = 120) private String origen;

    @ManyToOne @JoinColumn(name = "usuario_id") private Usuario usuario;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}