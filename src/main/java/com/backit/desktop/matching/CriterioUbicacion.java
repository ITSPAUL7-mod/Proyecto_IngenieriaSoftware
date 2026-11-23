package com.backit.desktop.matching;

import model.operativo.ObjetoEncontrado;
import model.operativo.ReportePerdida;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CriterioUbicacion implements CriterioMatching {

    @Override
    public String nombre() {
        return "UBICACION";
    }

    @Override
    public BigDecimal puntaje(ObjetoEncontrado o, ReportePerdida r) {
        if (o.getUbicacion() == null) {
            return BigDecimal.ZERO;
        }
        if (r.getUbicacionPerdida() != null
                && o.getUbicacion().getId().equals(r.getUbicacionPerdida().getId())) {
            return new BigDecimal("100");
        }
        if (o.getInstitucion() != null && r.getInstitucion() != null
                && o.getInstitucion().getId().equals(r.getInstitucion().getId())) {
            return new BigDecimal("40"); // misma sede, ubicacion exacta desconocida
        }
        return BigDecimal.ZERO;
    }
}
