package model.seguridad;


import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "institucion")
public class Institucion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150, nullable = false) private String nombre;
    @Column(length = 20, unique = true) private String ruc;
    @Column(length = 250) private String direccion;
    @Column(length = 30) private String telefono;
    @Column(name = "contacto_principal", length = 150) private String contactoPrincipal;
    @Column(columnDefinition = "boolean default true") private Boolean activo = true;
    @Column(name = "creado_en") private OffsetDateTime creadoEn;
}