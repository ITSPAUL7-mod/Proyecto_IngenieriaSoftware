package com.backit.desktop.audit;

import com.backit.desktop.repositories.AuditoriaRepository;
import com.backit.desktop.utils.Hashing;
import com.backit.desktop.utils.Origen;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.seguridad.Usuario;
import model.sistema.AuditoriaSistema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.TreeMap;


@Service
public class AuditoriaService {

    private final AuditoriaRepository repository;
    private final ObjectMapper mapper;

    public AuditoriaService(AuditoriaRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    @Transactional
    public AuditoriaSistema registrar(Usuario actor, String accion, String entidad,
                                      Object registroId, Map<String, Object> detalles) {
        OffsetDateTime ahora = OffsetDateTime.now();
        String json = serializar(detalles);
        String registro = registroId == null ? null : String.valueOf(registroId);

        AuditoriaSistema a = new AuditoriaSistema();
        a.setUsuario(actor);
        a.setAccion(accion);
        a.setEntidadAfectada(entidad);
        a.setRegistroId(registro);
        a.setOrigen(Origen.actual());
        a.setDetalles(json);
        a.setCreadoEn(ahora);
        a.setHashIntegridad(Hashing.sha256Hex(String.join("|",
                actor == null ? "-" : String.valueOf(actor.getId()),
                accion, entidad, registro == null ? "-" : registro,
                a.getOrigen(), json, ahora.toInstant().toString())));
        return repository.save(a);
    }

    private String serializar(Map<String, Object> detalles) {
        try {
            // TreeMap: orden de claves estable para que el hash sea reproducible.
            return mapper.writeValueAsString(detalles == null ? Map.of() : new TreeMap<>(detalles));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el detalle de auditoría", e);
        }
    }
}
