package com.backit.desktop.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@Configuration
@EntityScan(basePackages = "model")
@EnableJpaRepositories(basePackages = "com.backit.desktop.repositories")
public class JpaConfig {
}
