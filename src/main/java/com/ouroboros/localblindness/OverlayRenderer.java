package com.ouroboros.localblindness;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.function.Supplier;

/**
 * Draws the full-screen darkening overlay. This is the only Minecraft-facing piece of the effect; all
 * of the opacity math lives in {@link EffectMath}. Registered as the last HUD element so it paints on
 * top of the rest of the HUD, and is a no-op whenever the toggle is off.
 */
public final class OverlayRenderer implements HudElement {
    private final ToggleState toggle;
    private final Supplier<BlindnessConfig> config;
    private final long startNanos = System.nanoTime();

    public OverlayRenderer(ToggleState toggle, Supplier<BlindnessConfig> config) {
        this.toggle = toggle;
        this.config = config;
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!toggle.isEnabled()) {
            return;
        }
        BlindnessConfig cfg = config.get();
        float alpha;
        if (cfg.resolvedStyle() == EffectStyle.DARKNESS) {
            double seconds = (System.nanoTime() - startNanos) / 1_000_000_000.0;
            alpha = EffectMath.darknessAlpha(cfg.darknessBase, cfg.darknessAmplitude, cfg.darknessPeriodSeconds, seconds);
        } else {
            alpha = EffectMath.blindnessAlpha(cfg.blindnessIntensity);
        }
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        context.fill(0, 0, width, height, EffectMath.blackWithAlpha(alpha));
    }
}
