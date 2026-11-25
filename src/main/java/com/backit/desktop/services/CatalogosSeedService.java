package com.backit.desktop.services;

import com.backit.desktop.repositories.CategoriaObjetoRepository;
import model.operativo.CategoriaObjeto;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

//categorias iniciales del catalogo
@Service
public class CatalogosSeedService implements ApplicationRunner {

    private static final List<String> INICIALES =
            List.of("Electrónica", "Documentos", "Prendas", "Accesorios", "Otros");

    private final CategoriaObjetoRepository categorias;

    public CatalogosSeedService(CategoriaObjetoRepository categorias) {
        this.categorias = categorias;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String nombre : INICIALES) {
            if (categorias.findByNombreIgnoreCase(nombre).isPresent()) {
                continue;
            }
            CategoriaObjeto c = new CategoriaObjeto();
            c.setNombre(nombre);
            c.setActivo(true);
            c.setCreadoEn(OffsetDateTime.now());
            c.setActualizadoEn(OffsetDateTime.now());
            categorias.save(c);
        }
    }
}
