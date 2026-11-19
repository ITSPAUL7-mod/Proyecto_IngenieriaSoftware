package com.backit.desktop.matching;

import model.operativo.ObjetoEncontrado;
import model.operativo.ReportePerdida;

import java.math.BigDecimal;

/** Strategy: un criterio de puntuacion del motor (Sprint 5). */
public interface CriterioMatching {

    /** Clave del criterio, usada como nombre del peso en el motor. */
    String nombre();

    /** Puntaje entre 0 y 100. */
    BigDecimal puntaje(ObjetoEncontrado objeto, ReportePerdida reporte);
}
