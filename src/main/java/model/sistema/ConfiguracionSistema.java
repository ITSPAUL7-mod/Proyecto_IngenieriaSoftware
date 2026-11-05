package model.sistema;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Institucion;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "configuracion_sistema")
public class ConfiguracionSistema {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "institucion_id") private Institucion institucion;

    @Column(length = 100, nullable = false) private String clave;
    @Column(name = "valor_cifrado", columnDefinition = "TEXT", nullable = false) private String valorCifrado;
    @Column(length = 250) private String descripcion;
    @Column(length = 60, nullable = false) private String categoria;

    @ManyToOne @JoinColumn(name = "actualizado_por_id", nullable = false) private Usuario actualizadoPor;
    @Column(name = "actualizado_en") private OffsetDateTime actualizadoEn;
}