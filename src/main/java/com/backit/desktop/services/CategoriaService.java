package com.backit.desktop.services;

import com.backit.desktop.audit.AuditoriaService;
import com.backit.desktop.exceptions.ValidacionException;
import com.backit.desktop.repositories.CategoriaObjetoRepository;
import com.backit.desktop.security.AutorizacionService;
import com.backit.desktop.security.PermisoCodigo;
import com.backit.desktop.security.SesionActual;
import com.backit.desktop.utils.Validadores;
import model.operativo.CategoriaObjeto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

//catalogo de categorias de objetos
@Service
public class CategoriaService {

    private final CategoriaObjetoRepository categorias;
    private final AutorizacionService autorizacion;
    private final SesionActual sesion;
    private final AuditoriaService auditoria;

    public CategoriaService(CategoriaObjetoRepository categorias, AutorizacionService autorizacion,
                            SesionActual sesion, AuditoriaService auditoria) {
        this.categorias = categorias;
        this.autorizacion = autorizacion;
        this.sesion = sesion;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<CategoriaObjeto> listarActivas() {
        return categorias.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional
    public CategoriaObjeto crear(String nombre, String descripcion) {
        autorizacion.requerir(PermisoCodigo.CATEGORIA_GESTIONAR);
        String limpio = Validadores.texto(nombre, "Nombre de la categoría", 100);
        if (categorias.findByNombreIgnoreCase(limpio).isPresent()) {
            throw new ValidacionException("Ya existe esa categoría.");
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        CategoriaObjeto c = new CategoriaObjeto();
        c.setNombre(limpio);
        c.setDescripcion(descripcion == null || descripcion.isBlank()
                ? null : Validadores.texto(descripcion, "Descripción", 250));
        c.setActivo(true);
        c.setCreadoEn(ahora);
        c.setActualizadoEn(ahora);
        categorias.save(c);
        auditoria.registrar(sesion.requerirUsuario(), "CATEGORIA_CREAR", "categoria_objeto", c.getId(),
                Map.of("nombre", limpio));
        return c;
    }
}
