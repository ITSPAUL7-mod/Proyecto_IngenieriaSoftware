package model.seguridad;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "institucion_id") private Institucion institucion;
    @ManyToOne @JoinColumn(name = "rol_id", nullable = false) private Rol rol;

    @Column(length = 100, nullable = false) private String nombre;
    @Column(length = 100, nullable = false) private String apellido;
    @Column(length = 180, unique = true, nullable = false) private String correo;
    @Column(name = "password_hash", length = 100, nullable = false) private String passwordHash;
    @Column(name = "telefono_cifrado", columnDefinition = "TEXT") private String telefonoCifrado;
    @Column(length = 20, nullable = false) private String estado;

    @Column(name = "creado_en") private OffsetDateTime creadoEn;
    @Column(name = "actualizado_en") private OffsetDateTime actualizadoEn;
}