package com.ouroboros.blindfold;

/**
 * The on/off state of the effect for this game session. Session-only by design: it always starts
 * off and is never persisted. Marked volatile because it is written from the client-tick / command
 * path and read from the render path.
 */
public final class ToggleState {
    private volatile boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void set(boolean value) {
        this.enabled = value;
    }

    /** Flip the state and return the new value. */
    public boolean toggle() {
        boolean next = !enabled;
        this.enabled = next;
        return next;
    }
}
