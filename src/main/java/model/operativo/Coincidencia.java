package model.operativo;

import jakarta.persistence.*;
import lombok.Data;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "coincidencia")
public class Coincidencia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "objeto_encontrado_id", nullable = false) private ObjetoEncontrado objetoEncontrado;
    @ManyToOne @JoinColumn(name = "reporte_perdida_id", nullable = false) private ReportePerdida reportePerdida;

    @Column(name = "puntaje_categoria", precision = 5, scale = 2) private BigDecimal puntajeCategoria;
    @Column(name = "puntaje_descripcion", precision = 5, scale = 2) private BigDecimal puntajeDescripcion;
    @Column(name = "puntaje_ubicacion", precision = 5, scale = 2) private BigDecimal puntajeUbicacion;
    @Column(name = "puntaje_fecha", precision = 5, scale = 2) private BigDecimal puntajeFecha;
    @Column(name = "puntaje_caracteristicas", precision = 5, scale = 2) private BigDecimal puntajeCaracteristicas;
    @Column(name = "puntaje_total", precision = 5, scale = 2) private BigDecimal puntajeTotal;

    @Column(length = 30, nullable = false) private String estado;
    @ManyToOne @JoinColumn(name = "verificado_por_id") private Usuario verificadoPor;
    @Column(name = "fecha_coincidencia") private OffsetDateTime fechaCoincidencia;
}