package com.backit.desktop.matching;

import model.operativo.ObjetoEncontrado;
import model.operativo.ReportePerdida;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class CriterioDescripcion implements CriterioMatching {

    private static final Pattern NO_PALABRA = Pattern.compile("[^a-z0-9]+");
    private static final Set<String> STOPWORDS = Set.of(
            "de", "la", "el", "los", "las", "un", "una", "unos", "unas", "y", "o", "en", "con",
            "por", "para", "al", "del", "que", "se", "su", "sus", "es");

    @Override
    public String nombre() {
        return "DESCRIPCION";
    }

    @Override
    public BigDecimal puntaje(ObjetoEncontrado o, ReportePerdida r) {
        return similitud(o.getDescripcion(), r.getDescripcion());
    }

    static BigDecimal similitud(String a, String b) {
        Set<String> xa = tokens(a);
        Set<String> xb = tokens(b);
        if (xa.isEmpty() || xb.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Set<String> inter = new LinkedHashSet<>(xa);
        inter.retainAll(xb);
        Set<String> union = new LinkedHashSet<>(xa);
        union.addAll(xb);
        return BigDecimal.valueOf(inter.size() * 100.0 / union.size())
                .setScale(2, RoundingMode.HALF_UP);
    }

    static Set<String> tokens(String texto) {
        if (texto == null || texto.isBlank()) {
            return Set.of();
        }
        String normal = Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return Arrays.stream(NO_PALABRA.split(normal))
                .filter(t -> t.length() > 2 && !STOPWORDS.contains(t))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }
}
