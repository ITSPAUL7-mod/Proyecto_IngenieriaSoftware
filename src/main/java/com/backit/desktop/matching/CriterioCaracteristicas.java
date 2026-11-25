package com.backit.desktop.matching;

import model.operativo.ObjetoEncontrado;
import model.operativo.ReportePerdida;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class CriterioCaracteristicas implements CriterioMatching {

    @Override
    public String nombre() {
        return "CARACTERISTICAS";
    }

    @Override
    public BigDecimal puntaje(ObjetoEncontrado o, ReportePerdida r) {
        return CriterioDescripcion.similitud(o.getCaracteristicasPrivadas(), r.getCaracteristicasPrivadas());
    }
}
