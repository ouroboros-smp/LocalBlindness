package com.ouroboros.localblindness;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToggleStateTest {

    @Test
    void startsDisabled() {
        assertFalse(new ToggleState().isEnabled());
    }

    @Test
    void toggleFlipsAndReturnsNewValue() {
        ToggleState state = new ToggleState();
        assertTrue(state.toggle());
        assertTrue(state.isEnabled());
        assertFalse(state.toggle());
        assertFalse(state.isEnabled());
    }

    @Test
    void setForcesValue() {
        ToggleState state = new ToggleState();
        state.set(true);
        assertTrue(state.isEnabled());
        state.set(false);
        assertFalse(state.isEnabled());
    }
}
