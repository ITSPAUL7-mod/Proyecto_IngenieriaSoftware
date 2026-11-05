package model.seguridad;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "dispositivo_usuario")
public class DispositivoUsuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "usuario_id", nullable = false) private Usuario usuario;

    @Column(name = "fingerprint_hash", length = 128, nullable = false) private String fingerprintHash;
    @Column(name = "nombre_dispositivo", length = 120) private String nombreDispositivo;
    @Column(length = 20, nullable = false) private String estado;
    @Column(name = "ultimo_acceso") private OffsetDateTime ultimoAcceso;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
    @Column(name = "revocado_en") private OffsetDateTime revocadoEn;
}