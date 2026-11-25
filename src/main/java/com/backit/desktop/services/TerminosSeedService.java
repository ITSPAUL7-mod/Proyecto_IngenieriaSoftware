package com.backit.desktop.services;

import com.backit.desktop.repositories.TerminoCondicionRepository;
import com.backit.desktop.utils.Hashing;
import model.sistema.TerminoCondicion;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

//Terminos iniciales que van dentro de la interfaz de JavaFX
@Service
public class TerminosSeedService implements ApplicationRunner {

    public static final String VERSION_INICIAL = "1.0";

    private static final String CONTENIDO = """
            TÉRMINOS Y CONDICIONES DE USO Y POLÍTICA DE PRIVACIDAD — BackIt

            1. Información que se recopila.
            Se registran únicamente los datos necesarios para identificar, reclamar y devolver
            un objeto: nombre y correo del usuario, institución, título y descripción pública
            del hallazgo o reporte, fecha, ubicación aproximada y, cuando se aportan, señas
            privadas y evidencias. La contraseña nunca se almacena en texto claro: se guarda
            solo un hash BCrypt.

            2. Finalidad.
            La información se usa exclusivamente para la custodia, búsqueda, verificación de
            propiedad y devolución documentada de objetos perdidos dentro de la institución.

            3. Datos públicos y privados.
            Las señas de verificación y las evidencias son privadas: no se muestran en
            consultas generales y nunca se codifican dentro del código QR. El QR contiene
            únicamente un token aleatorio (UUID) sin datos personales.

            4. Quién puede consultar la información.
            Solo los perfiles autorizados (SUPERADMIN, AUDITOR, ADMIN_SEDE y
            PERSONAL_CUSTODIA, según corresponda) pueden acceder a los registros, y siempre
            bajo control de acceso por roles (RBAC). El usuario final solo ve sus propios
            reportes, coincidencias y reclamaciones.

            5. Conservación e integridad.
            Cada acción sensible genera una bitácora inmutable con hash SHA-256. Las evidencias
            y fotografías se guardan en una carpeta privada con nombre aleatorio y se les
            calcula un hash SHA-256 para detectar alteraciones.

            6. Responsabilidades del usuario.
            El usuario se compromete a proporcionar información veraz, a no registrar objetos
            ajenos como propios, a presentar evidencias legítimas y a respetar los plazos y
            procedimientos de custodia de la institución. La reclamación requiere siempre
            verificación humana antes de autorizar una entrega.

            7. Aceptación versionada.
            La aceptación de esta versión queda registrada con usuario, fecha, dispositivo y
            hash del contenido. Cuando el texto cambie, se exigirá una nueva aceptación antes
            de continuar usando el sistema.

            8. Revocación y salida.
            Si no acepta estos términos puede salir de la aplicación sin acceder al menú
            principal. La revocación posterior debe solicitarla al administrador institucional.
            """;

    private final TerminoCondicionRepository terminos;

    public TerminosSeedService(TerminoCondicionRepository terminos) {
        this.terminos = terminos;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (terminos.findByVersion(VERSION_INICIAL).isPresent()) {
            return;
        }
        TerminoCondicion t = new TerminoCondicion();
        t.setVersion(VERSION_INICIAL);
        t.setTitulo("Términos y condiciones de uso y privacidad");
        t.setContenido(CONTENIDO);
        t.setContenidoHash(Hashing.sha256Hex(CONTENIDO));
        t.setFechaVigencia(OffsetDateTime.now());
        t.setActivo(true);
        t.setCreadoEn(OffsetDateTime.now());
        terminos.save(t);
    }
}
