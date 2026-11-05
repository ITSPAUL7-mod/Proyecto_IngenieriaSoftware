package model.sistema;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import model.seguridad.Usuario;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "auditoria_sistema")
public class AuditoriaSistema {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "usuario_id") private Usuario usuario;

    @Column(length = 80, nullable = false) private String accion;
    @Column(name = "entidad_afectada", length = 80, nullable = false) private String entidadAfectada;
    @Column(name = "registro_id", length = 80) private String registroId;
    @Column(length = 120) private String origen;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "JSONB") private String detalles;
    @Column(name = "hash_integridad", length = 64, nullable = false) private String hashIntegridad;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}