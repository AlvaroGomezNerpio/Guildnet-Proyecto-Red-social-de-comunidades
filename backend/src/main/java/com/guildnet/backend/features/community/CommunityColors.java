package com.guildnet.backend.features.community;

import java.util.Locale;

/** Aspecto de una comunidad: colores (también de sus etiquetas) y tipografía. */
public final class CommunityColors {

    /** Color en formato hexadecimal de seis cifras: #rrggbb. */
    public static final String HEX_PATTERN = "^#[0-9a-fA-F]{6}$";

    public static final String DEFAULT_PRIMARY = "#7c3aed";
    public static final String DEFAULT_ACCENT = "#06b6d4";

    /** Tipografías disponibles para los títulos de la comunidad. */
    public static final String HEADING_FONT_PATTERN = "^(default|serif|rounded|pixel|hand)$";
    public static final String DEFAULT_HEADING_FONT = "default";

    /** Casi negro usado como texto sobre fondos claros. */
    private static final String INK = "#111322";

    private CommunityColors() {
    }

    /** Devuelve el color en minúsculas, o {@code fallback} si no se ha indicado ninguno. */
    public static String normalize(String color, String fallback) {
        if (color == null || color.isBlank()) {
            return fallback;
        }
        return color.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Texto que mejor se lee sobre un color de fondo: blanco o casi negro, el que
     * más contraste dé (relación de contraste WCAG). Mismo criterio que el frontend.
     */
    public static String readableOn(String background) {
        double bg = luminance(background);
        double withWhite = (1.0 + 0.05) / (bg + 0.05);
        double withInk = (bg + 0.05) / (luminance(INK) + 0.05);
        return withWhite >= withInk ? "#ffffff" : INK;
    }

    private static double luminance(String hex) {
        int rgb = Integer.parseInt(hex.substring(1), 16);
        double r = channel((rgb >> 16) & 255);
        double g = channel((rgb >> 8) & 255);
        double b = channel(rgb & 255);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double channel(int value) {
        double s = value / 255.0;
        return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
    }
}
