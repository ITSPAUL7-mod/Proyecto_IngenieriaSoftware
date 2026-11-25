package com.backit.desktop.matching;

import model.operativo.ObjetoEncontrado;
import model.operativo.ReportePerdida;

import java.math.BigDecimal;

public interface CriterioMatching {

    String nombre();

    BigDecimal puntaje(ObjetoEncontrado objeto, ReportePerdida reporte);
}
