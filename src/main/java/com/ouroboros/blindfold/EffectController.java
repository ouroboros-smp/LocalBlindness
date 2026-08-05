package com.ouroboros.blindfold;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.function.Supplier;

/**
 * Keeps the real client-side Blindness (or Darkness) status effect in sync with the toggle. Meant to
 * be called every client tick.
 *
 * <p>While enabled it asserts an infinite-duration effect on the local player, re-adding it if it is
 * ever missing (for example after a respawn or dimension change), so the server's periodic effect
 * sync cannot strip it. While disabled it clears the effect it manages. This is the genuine vanilla
 * effect, not an overlay, so it produces the real blindness fog and darkening. Everything happens on
 * the client; the server is never told.
 */
public final class EffectController {
    private final ToggleState toggle;
    private final Supplier<BlindnessConfig> config;

    /** True once we have applied an effect this session; gates removal so we do not touch effects we did not add. */
    private boolean applied;

    public EffectController(ToggleState toggle, Supplier<BlindnessConfig> config) {
        this.toggle = toggle;
        this.config = config;
    }

    public void tick(LocalPlayer player) {
        if (player == null) {
            return;
        }
        BlindnessConfig cfg = config.get();
        boolean darkness = cfg.resolvedStyle() == EffectStyle.DARKNESS;
        Holder<MobEffect> wanted = darkness ? MobEffects.DARKNESS : MobEffects.BLINDNESS;
        Holder<MobEffect> other = darkness ? MobEffects.BLINDNESS : MobEffects.DARKNESS;

        if (toggle.isEnabled()) {
            MobEffectInstance current = player.getEffect(wanted);
            if (current == null || !current.isInfiniteDuration()) {
                player.addEffect(new MobEffectInstance(
                        wanted, MobEffectInstance.INFINITE_DURATION, 0, false, false, cfg.showEffectIcon));
                applied = true;
            }
            // If the style was switched while active, drop the effect we previously applied.
            if (applied && player.hasEffect(other)) {
                player.removeEffect(other);
            }
        } else if (applied) {
            player.removeEffect(MobEffects.BLINDNESS);
            player.removeEffect(MobEffects.DARKNESS);
            applied = false;
        }
    }
}
