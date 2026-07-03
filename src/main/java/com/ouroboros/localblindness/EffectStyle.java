package com.ouroboros.localblindness;

import java.util.Locale;

/** The visual style the overlay draws. */
public enum EffectStyle {
    /** Constant heavy darkening, mimicking the vanilla Blindness effect. */
    BLINDNESS,
    /** Slow pulsing dim, mimicking the Warden's Darkness effect. */
    DARKNESS;

    /**
     * Parse a style by name, case-insensitive.
     *
     * @param raw      the input string (may be null)
     * @param fallback returned when {@code raw} is null or does not name a style
     */
    public static EffectStyle fromString(String raw, EffectStyle fallback) {
        if (raw == null) {
            return fallback;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (EffectStyle style : values()) {
            if (style.name().equals(normalized)) {
                return style;
            }
        }
        return fallback;
    }
}
