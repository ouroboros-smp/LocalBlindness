package com.ouroboros.localblindness;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlindnessConfigTest {

    @Test
    void defaultsAreSane() {
        BlindnessConfig cfg = new BlindnessConfig();
        assertEquals(EffectStyle.BLINDNESS, cfg.resolvedStyle());
        assertTrue(cfg.blindnessIntensity >= 0.0f && cfg.blindnessIntensity <= 1.0f);
    }

    @Test
    void clampSelfBoundsOpacitiesAndPeriod() {
        BlindnessConfig cfg = new BlindnessConfig();
        cfg.blindnessIntensity = 5.0f;
        cfg.darknessBase = -1.0f;
        cfg.darknessAmplitude = 9.0f;
        cfg.darknessPeriodSeconds = 0.0f;

        cfg.clampSelf();

        assertEquals(1.0f, cfg.blindnessIntensity);
        assertEquals(0.0f, cfg.darknessBase);
        assertEquals(1.0f, cfg.darknessAmplitude);
        assertTrue(cfg.darknessPeriodSeconds >= 0.1f, "period floored");
    }

    @Test
    void clampSelfNormalizesBadStyleToBlindness() {
        BlindnessConfig cfg = new BlindnessConfig();
        cfg.style = "not-a-real-style";
        cfg.clampSelf();
        assertEquals(EffectStyle.BLINDNESS.name(), cfg.style);
    }

    @Test
    void resolvedStyleReadsDarkness() {
        BlindnessConfig cfg = new BlindnessConfig();
        cfg.style = "darkness";
        assertEquals(EffectStyle.DARKNESS, cfg.resolvedStyle());
    }
}
