package com.backit.desktop.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Las entidades viven en el paquete raíz "model", fuera de com.backit.desktop, por lo que
 * @SpringBootApplication no las detecta por sí solo: sin @EntityScan Hibernate no crearía las tablas.
 */
@Configuration
@EntityScan(basePackages = "model")
@EnableJpaRepositories(basePackages = "com.backit.desktop.repositories")
public class JpaConfig {
}
