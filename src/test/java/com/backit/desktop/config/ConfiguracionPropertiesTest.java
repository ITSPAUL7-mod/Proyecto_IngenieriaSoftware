package com.backit.desktop.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que ${DB_URL}, ${DB_USER} y ${DB_PASSWORD} se resuelven desde el
 * entorno o desde application-local.properties, sin llegar a abrir JPA.
 */
class ConfiguracionPropertiesTest {

    @Configuration
    static class SoloConfiguracion {
    }

    @Test
    void losPlaceholdersDeBaseDeDatosSeResuelven() {
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(SoloConfiguracion.class)
                .web(WebApplicationType.NONE)
                .run()) {
            String url = ctx.getEnvironment().getProperty("DB_URL");
            assertNotNull(url, "Defina DB_URL o copie application-local.properties.example");
            assertTrue(url.startsWith("jdbc:postgresql://"), "URL no resuelta: " + url);
            assertNotNull(ctx.getEnvironment().getProperty("DB_USER"));
            assertNotNull(ctx.getEnvironment().getProperty("DB_PASSWORD"));
        }
    }
}
