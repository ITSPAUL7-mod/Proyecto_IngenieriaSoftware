package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.exceptions.AccesoDenegadoException;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.NotificacionRepository;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.utils.Validadores;
import model.seguridad.Usuario;
import model.sistema.Notificacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/** Sprint 5 - notificaciones internas (MATCH, RECLAMO, ENTREGA, SISTEMA). */
@Service
public class NotificacionService {

    private final NotificacionRepository notificaciones;
    private final SesionActual sesion;
    private final AuditoriaService auditoria;

    public NotificacionService(NotificacionRepository notificaciones, SesionActual sesion,
                               AuditoriaService auditoria) {
        this.notificaciones = notificaciones;
        this.sesion = sesion;
        this.auditoria = auditoria;
    }

    @Transactional
    public Notificacion notificar(Usuario destinatario, String titulo, String mensaje, String tipo) {
        if (destinatario == null) {
            throw new ValidacionException("La notificación necesita un destinatario.");
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        Notificacion n = new Notificacion();
        n.setUsuario(destinatario);
        n.setTitulo(Validadores.texto(titulo, "Título de la notificación", 150));
        n.setMensaje(Validadores.texto(mensaje, "Mensaje", 4000));
        n.setTipo(tipo);
        n.setLeida(false);
        n.setFechaEnvio(ahora);
        n.setCreadoEn(ahora);
        notificaciones.save(n);

        auditoria.registrar(sesion.usuario().orElse(null), "NOTIFICACION_CREAR", "notificacion", n.getId(),
                Map.of("tipo", tipo, "destinatarioId", destinatario.getId()));
        return n;
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listarPropias() {
        return notificaciones.findByUsuarioIdOrderByFechaEnvioDesc(sesion.requerirUsuario().getId());
    }

    @Transactional
    public void marcarLeida(Long id) {
        Notificacion n = notificaciones.findById(id)
                .orElseThrow(() -> new ValidacionException("La notificación no existe."));
        if (!n.getUsuario().getId().equals(sesion.requerirUsuario().getId())) {
            throw new AccesoDenegadoException("La notificación pertenece a otro usuario.");
        }
        n.setLeida(true);
        notificaciones.save(n);
    }
}
