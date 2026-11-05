package model.reclamacion;

import jakarta.persistence.*;
import lombok.Data;
import model.operativo.Coincidencia;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "reclamacion")
public class Reclamacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne @JoinColumn(name = "coincidencia_id", unique = true, nullable = false) private Coincidencia coincidencia;
    @ManyToOne @JoinColumn(name = "usuario_solicitante_id", nullable = false) private Usuario usuarioSolicitante;

    @Column(length = 30, nullable = false) private String estado;
    @Column(name = "motivo_rechazo", columnDefinition = "TEXT") private String motivoRechazo;
    @Column(name = "fecha_solicitud") private OffsetDateTime fechaSolicitud;
    @Column(name = "fecha_resolucion") private OffsetDateTime fechaResolucion;

    @ManyToOne @JoinColumn(name = "atendido_por_id") private Usuario atendidoPor;
    @Column(name = "documento_verificado_hash", length = 128) private String documentoVerificadoHash;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}