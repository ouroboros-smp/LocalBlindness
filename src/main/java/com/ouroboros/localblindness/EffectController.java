package com.ouroboros.localblindness;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;

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

    public void tick(ClientPlayerEntity player) {
        if (player == null) {
            return;
        }
        BlindnessConfig cfg = config.get();
        boolean darkness = cfg.resolvedStyle() == EffectStyle.DARKNESS;
        RegistryEntry<StatusEffect> wanted = darkness ? StatusEffects.DARKNESS : StatusEffects.BLINDNESS;
        RegistryEntry<StatusEffect> other = darkness ? StatusEffects.BLINDNESS : StatusEffects.DARKNESS;

        if (toggle.isEnabled()) {
            StatusEffectInstance current = player.getStatusEffect(wanted);
            if (current == null || !current.isInfinite()) {
                player.addStatusEffect(new StatusEffectInstance(
                        wanted, StatusEffectInstance.INFINITE, 0, false, false, cfg.showEffectIcon));
                applied = true;
            }
            // If the style was switched while active, drop the effect we previously applied.
            if (applied && player.hasStatusEffect(other)) {
                player.removeStatusEffect(other);
            }
        } else if (applied) {
            player.removeStatusEffect(StatusEffects.BLINDNESS);
            player.removeStatusEffect(StatusEffects.DARKNESS);
            applied = false;
        }
    }
}
