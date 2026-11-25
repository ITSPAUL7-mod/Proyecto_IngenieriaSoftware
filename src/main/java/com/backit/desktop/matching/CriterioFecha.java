package com.backit.desktop.matching;

import model.operativo.ObjetoEncontrado;
import model.operativo.ReportePerdida;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Component
public class CriterioFecha implements CriterioMatching {

    @Override
    public String nombre() {
        return "FECHA";
    }

    @Override
    public BigDecimal puntaje(ObjetoEncontrado o, ReportePerdida r) {
        if (o.getFechaEncontrado() == null || r.getFechaPerdida() == null) {
            return BigDecimal.ZERO;
        }
        long dias = Math.abs(ChronoUnit.DAYS.between(
                o.getFechaEncontrado().toLocalDate(), r.getFechaPerdida().toLocalDate()));
        return BigDecimal.valueOf(Math.max(0, 100 - dias * 5));
    }
}
