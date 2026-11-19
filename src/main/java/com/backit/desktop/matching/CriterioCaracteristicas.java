package com.backit.desktop.matching;

import model.operativo.ObjetoEncontrado;
import model.operativo.ReportePerdida;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Compara las señas privadas. El resultado es un puntaje mas, NO se muestra al
 * usuario en las consultas generales ni se incluye en el QR (informe tecnico).
 */
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
