package com.guildnet.backend.common.persistence;

import java.util.Locale;

/** Construye patrones LIKE seguros (escapando los comodines que escriba el usuario). */
public final class LikePatterns {

    public static final char ESCAPE = '!';

    private LikePatterns() {
    }

    /** Patrón "contiene", en minúsculas, para usar con {@code cb.like(cb.lower(expr), pattern, ESCAPE)}. */
    public static String contains(String value) {
        String escaped = value.trim().toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + escaped + "%";
    }
}
