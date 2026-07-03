package com.ouroboros.localblindness;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectMathTest {

    @Test
    void clamp01BoundsValues() {
        assertEquals(0.0f, EffectMath.clamp01(-0.5f));
        assertEquals(1.0f, EffectMath.clamp01(2.0f));
        assertEquals(0.42f, EffectMath.clamp01(0.42f));
    }

    @Test
    void blindnessAlphaIsClampedIntensity() {
        assertEquals(0.9f, EffectMath.blindnessAlpha(0.9f));
        assertEquals(1.0f, EffectMath.blindnessAlpha(5.0f));
        assertEquals(0.0f, EffectMath.blindnessAlpha(-1.0f));
    }

    @Test
    void darknessAlphaSitsAtBaseWhenTimeIsZero() {
        // sin(0) == 0, so the value is exactly the base.
        assertEquals(0.55f, EffectMath.darknessAlpha(0.55f, 0.30f, 3.0f, 0.0), 1e-6);
    }

    @Test
    void darknessAlphaPeaksAtQuarterPeriod() {
        // sin(pi/2) == 1, so we get base + amplitude at t = period/4.
        float v = EffectMath.darknessAlpha(0.50f, 0.30f, 4.0f, 1.0);
        assertEquals(0.80f, v, 1e-5);
    }

    @Test
    void darknessAlphaStaysWithinUnitRange() {
        for (double t = 0.0; t < 10.0; t += 0.05) {
            float v = EffectMath.darknessAlpha(0.9f, 0.9f, 2.0f, t);
            assertTrue(v >= 0.0f && v <= 1.0f, "alpha out of range at t=" + t + ": " + v);
        }
    }

    @Test
    void darknessAlphaWithNonPositivePeriodIsConstant() {
        assertEquals(0.4f, EffectMath.darknessAlpha(0.4f, 0.3f, 0.0f, 12.34), 1e-6);
        assertEquals(0.4f, EffectMath.darknessAlpha(0.4f, 0.3f, -1.0f, 99.9), 1e-6);
    }

    @Test
    void blackWithAlphaPacksArgb() {
        assertEquals(0x00000000, EffectMath.blackWithAlpha(0.0f));
        assertEquals(0xFF000000, EffectMath.blackWithAlpha(1.0f));
        // 0.5 * 255 rounds to 128 (0x80) in the alpha byte, RGB stays 0.
        assertEquals(0x80000000, EffectMath.blackWithAlpha(0.5f));
    }
}
