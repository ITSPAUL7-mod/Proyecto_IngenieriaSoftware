package com.backit.desktop.repositories;

import model.sistema.AuditoriaSistema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<AuditoriaSistema, Long> {
}
