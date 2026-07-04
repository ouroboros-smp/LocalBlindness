package com.ouroboros.localblindness;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * User-editable settings, persisted as JSON in the Fabric config directory. Only the effect style,
 * the default keybind, and whether to show the effect icon persist. The on/off toggle is deliberately
 * session-only (see {@link ToggleState}).
 *
 * <p>Kept free of Minecraft types so it can be unit tested on a plain JVM.
 */
public final class BlindnessConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("localblindness");

    /** Which real status effect the toggle applies ("BLINDNESS" or "DARKNESS"). */
    public String style = EffectStyle.BLINDNESS.name();

    /** GLFW key code the toggle keybind defaults to (66 == 'B'). Players can rebind it in Controls. */
    public int toggleKeyCode = 66;

    /** Show the effect's icon in the HUD/inventory while active. Off by default to keep the screen clean. */
    public boolean showEffectIcon = false;

    /** Resolve the parsed style, defaulting to BLINDNESS if the stored string is bad. */
    public EffectStyle resolvedStyle() {
        return EffectStyle.fromString(style, EffectStyle.BLINDNESS);
    }

    /** Normalize fields to sane values. Returns {@code this} for chaining. */
    public BlindnessConfig clampSelf() {
        this.style = resolvedStyle().name();
        return this;
    }

    // ---- persistence ----

    /** Load config from disk (or defaults if missing/unreadable), normalize it, and write it back. */
    public static BlindnessConfig load(Path path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        BlindnessConfig config = new BlindnessConfig();
        if (Files.exists(path)) {
            try {
                BlindnessConfig parsed = gson.fromJson(Files.readString(path), BlindnessConfig.class);
                if (parsed != null) {
                    config = parsed;
                }
            } catch (Exception e) {
                LOGGER.warn("[LocalBlindness] Could not read config {}, using defaults", path, e);
            }
        }
        config.clampSelf();
        config.save(path);
        return config;
    }

    /** Write this config to disk as pretty JSON. */
    public void save(Path path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, gson.toJson(this));
        } catch (IOException e) {
            LOGGER.warn("[LocalBlindness] Could not write config {}", path, e);
        }
    }
}
