package com.ouroboros.blindfold;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BlindnessConfigTest {

    @Test
    void defaultsAreSane() {
        BlindnessConfig cfg = new BlindnessConfig();
        assertEquals(EffectStyle.BLINDNESS, cfg.resolvedStyle());
        assertEquals(66, cfg.toggleKeyCode);
        assertFalse(cfg.showEffectIcon);
    }

    @Test
    void clampSelfNormalizesBadStyleToBlindness() {
        BlindnessConfig cfg = new BlindnessConfig();
        cfg.style = "not-a-real-style";
        cfg.clampSelf();
        assertEquals(EffectStyle.BLINDNESS.name(), cfg.style);
    }

    @Test
    void clampSelfCanonicalizesStyleCasing() {
        BlindnessConfig cfg = new BlindnessConfig();
        cfg.style = "darkness";
        cfg.clampSelf();
        assertEquals(EffectStyle.DARKNESS.name(), cfg.style);
    }

    @Test
    void resolvedStyleReadsDarkness() {
        BlindnessConfig cfg = new BlindnessConfig();
        cfg.style = "darkness";
        assertEquals(EffectStyle.DARKNESS, cfg.resolvedStyle());
    }
}
