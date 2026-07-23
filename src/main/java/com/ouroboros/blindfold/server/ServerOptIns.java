package com.ouroboros.blindfold.server;

import com.ouroboros.blindfold.EffectStyle;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player opt-in state for the server side, keyed by player UUID. Opting in is a personal
 * choice made with the {@code /blindfold} command; nothing is ever forced on a player.
 *
 * <p>State is session-only by design, mirroring the client toggle: it lives in memory for the
 * lifetime of the server process (so it survives a relog) and is never written to disk. A player's
 * style choice is also per-session; while unset, the server config's default style applies.
 *
 * <p>Kept free of Minecraft types so it can be unit tested on a plain JVM.
 */
public final class ServerOptIns {
    private static final class Entry {
        volatile boolean enabled;
        /** Personal style override, or null to follow the server default. */
        volatile EffectStyle style;
    }

    private final Map<UUID, Entry> players = new ConcurrentHashMap<>();

    public boolean isEnabled(UUID player) {
        Entry entry = players.get(player);
        return entry != null && entry.enabled;
    }

    public void set(UUID player, boolean enabled) {
        entry(player).enabled = enabled;
    }

    /** Flip the player's opt-in state and return the new value. */
    public boolean toggle(UUID player) {
        Entry entry = entry(player);
        boolean next = !entry.enabled;
        entry.enabled = next;
        return next;
    }

    /** The style to apply for this player: their own choice if they made one, else {@code fallback}. */
    public EffectStyle styleFor(UUID player, EffectStyle fallback) {
        Entry entry = players.get(player);
        return entry == null || entry.style == null ? fallback : entry.style;
    }

    public void setStyle(UUID player, EffectStyle style) {
        entry(player).style = style;
    }

    private Entry entry(UUID player) {
        return players.computeIfAbsent(player, id -> new Entry());
    }
}
