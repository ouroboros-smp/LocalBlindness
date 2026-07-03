package com.ouroboros.localblindness;

/**
 * Pure, side-effect-free math for the screen overlay. Deliberately free of Minecraft types so it can
 * be unit tested on a plain JVM.
 */
public final class EffectMath {
    private EffectMath() {
    }

    /** Clamp a value into the inclusive range [0, 1]. */
    public static float clamp01(float value) {
        if (value < 0.0f) {
            return 0.0f;
        }
        if (value > 1.0f) {
            return 1.0f;
        }
        return value;
    }

    /** Blindness is a constant darkening; the alpha is just the (clamped) configured intensity. */
    public static float blindnessAlpha(float intensity) {
        return clamp01(intensity);
    }

    /**
     * Darkness pulses smoothly around {@code base} by +/- {@code amplitude} on a sine wave.
     *
     * @param base          midpoint opacity (0..1)
     * @param amplitude     how far opacity swings above/below the base (0..1)
     * @param periodSeconds seconds for one full pulse; values &lt;= 0 disable the pulse
     * @param timeSeconds   elapsed time driving the wave
     * @return clamped opacity in [0, 1]
     */
    public static float darknessAlpha(float base, float amplitude, float periodSeconds, double timeSeconds) {
        if (periodSeconds <= 0.0f) {
            return clamp01(base);
        }
        double phase = (2.0 * Math.PI / periodSeconds) * timeSeconds;
        float value = (float) (base + amplitude * Math.sin(phase));
        return clamp01(value);
    }

    /** Pack an opaque-black colour with the given alpha (0..1) into 0xAARRGGBB. */
    public static int blackWithAlpha(float alpha) {
        int a = Math.round(clamp01(alpha) * 255.0f);
        return a << 24;
    }
}
