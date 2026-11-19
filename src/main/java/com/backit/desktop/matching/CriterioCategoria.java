package com.backit.desktop.matching;

import model.operativo.ObjetoEncontrado;
import model.operativo.ReportePerdida;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CriterioCategoria implements CriterioMatching {

    @Override
    public String nombre() {
        return "CATEGORIA";
    }

    @Override
    public BigDecimal puntaje(ObjetoEncontrado o, ReportePerdida r) {
        if (o.getCategoria() == null || r.getCategoria() == null) {
            return BigDecimal.ZERO;
        }
        return o.getCategoria().getId().equals(r.getCategoria().getId())
                ? new BigDecimal("100") : BigDecimal.ZERO;
    }
}
