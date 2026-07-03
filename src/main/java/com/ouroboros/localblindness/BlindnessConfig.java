package com.ouroboros.localblindness;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * User-editable settings, persisted as JSON in the Fabric config directory. Only the effect style and
 * its visual intensities persist. The on/off toggle is deliberately session-only (see {@link ToggleState}).
 *
 * <p>Kept free of Minecraft types so the clamping and parsing logic can be unit tested on a plain JVM.
 */
public final class BlindnessConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("localblindness");

    /** Which style the overlay draws ("BLINDNESS" or "DARKNESS"). */
    public String style = EffectStyle.BLINDNESS.name();

    /** Blindness opacity, 0..1. Near-opaque by default. */
    public float blindnessIntensity = 0.90f;

    /** Darkness pulse midpoint opacity, 0..1. */
    public float darknessBase = 0.55f;

    /** Darkness pulse swing above/below the base, 0..1. */
    public float darknessAmplitude = 0.30f;

    /** Seconds per darkness pulse. */
    public float darknessPeriodSeconds = 3.0f;

    /** GLFW key code the toggle keybind defaults to (66 == 'B'). Players can rebind it in Controls. */
    public int toggleKeyCode = 66;

    /** Resolve the parsed style, defaulting to BLINDNESS if the stored string is bad. */
    public EffectStyle resolvedStyle() {
        return EffectStyle.fromString(style, EffectStyle.BLINDNESS);
    }

    /** Clamp every field into a sane range. Returns {@code this} for chaining. */
    public BlindnessConfig clampSelf() {
        this.style = resolvedStyle().name();
        this.blindnessIntensity = EffectMath.clamp01(blindnessIntensity);
        this.darknessBase = EffectMath.clamp01(darknessBase);
        this.darknessAmplitude = EffectMath.clamp01(darknessAmplitude);
        if (this.darknessPeriodSeconds < 0.1f) {
            this.darknessPeriodSeconds = 0.1f;
        }
        if (this.darknessPeriodSeconds > 60.0f) {
            this.darknessPeriodSeconds = 60.0f;
        }
        return this;
    }

    // ---- persistence ----

    /** Load config from disk (or defaults if missing/unreadable), clamp it, and write it back. */
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
