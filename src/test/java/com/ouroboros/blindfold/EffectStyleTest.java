package com.ouroboros.blindfold;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EffectStyleTest {

    @Test
    void parsesCaseInsensitively() {
        assertEquals(EffectStyle.DARKNESS, EffectStyle.fromString("darkness", EffectStyle.BLINDNESS));
        assertEquals(EffectStyle.DARKNESS, EffectStyle.fromString("  DaRkNeSs ", EffectStyle.BLINDNESS));
        assertEquals(EffectStyle.BLINDNESS, EffectStyle.fromString("BLINDNESS", EffectStyle.DARKNESS));
    }

    @Test
    void fallsBackForNullOrUnknown() {
        assertEquals(EffectStyle.BLINDNESS, EffectStyle.fromString(null, EffectStyle.BLINDNESS));
        assertEquals(EffectStyle.DARKNESS, EffectStyle.fromString("nonsense", EffectStyle.DARKNESS));
        assertEquals(EffectStyle.BLINDNESS, EffectStyle.fromString("", EffectStyle.BLINDNESS));
    }
}
