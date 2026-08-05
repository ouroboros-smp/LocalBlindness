package com.ouroboros.blindfold.server;

import com.ouroboros.blindfold.EffectStyle;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerOptInsTest {
    private final ServerOptIns optIns = new ServerOptIns();
    private final UUID alice = UUID.nameUUIDFromBytes("alice".getBytes());
    private final UUID bob = UUID.nameUUIDFromBytes("bob".getBytes());

    @Test
    void playersStartOptedOut() {
        assertFalse(optIns.isEnabled(alice));
    }

    @Test
    void setAndToggleArePerPlayer() {
        optIns.set(alice, true);
        assertTrue(optIns.isEnabled(alice));
        assertFalse(optIns.isEnabled(bob));

        assertTrue(optIns.toggle(bob));
        assertFalse(optIns.toggle(bob));
        assertTrue(optIns.isEnabled(alice));
    }

    @Test
    void styleFallsBackToServerDefaultUntilChosen() {
        assertEquals(EffectStyle.BLINDNESS, optIns.styleFor(alice, EffectStyle.BLINDNESS));
        assertEquals(EffectStyle.DARKNESS, optIns.styleFor(alice, EffectStyle.DARKNESS));

        optIns.setStyle(alice, EffectStyle.DARKNESS);
        assertEquals(EffectStyle.DARKNESS, optIns.styleFor(alice, EffectStyle.BLINDNESS));
        assertEquals(EffectStyle.BLINDNESS, optIns.styleFor(bob, EffectStyle.BLINDNESS));
    }

    @Test
    void styleChoiceSurvivesTogglingOnAndOff() {
        optIns.setStyle(alice, EffectStyle.DARKNESS);
        optIns.set(alice, true);
        optIns.set(alice, false);
        assertEquals(EffectStyle.DARKNESS, optIns.styleFor(alice, EffectStyle.BLINDNESS));
    }
}
