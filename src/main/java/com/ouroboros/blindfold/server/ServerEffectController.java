package com.ouroboros.blindfold.server;

import com.ouroboros.blindfold.BlindnessConfig;
import com.ouroboros.blindfold.EffectStyle;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Keeps the real server-side Blindness (or Darkness) status effect on every opted-in player. Meant
 * to be called every server tick.
 *
 * <p>Unlike the client half (which uses an infinite effect it fully controls), the server applies a
 * <em>short, finite</em> effect and refreshes it well before it runs out. That makes the whole
 * feature self-healing: if the server crashes or the mod is removed, the worst a player is left
 * with is a few seconds of leftover effect that expires on its own, and nothing permanent is ever
 * written into player data. {@link #onJoin} additionally clears such leftovers immediately for
 * players who are no longer opted in.
 *
 * <p>Effects the controller did not apply are left alone: an infinite Blindness from a command or
 * another mod is never touched, and gameplay effects are only swept up in the same narrow cases the
 * client half documents.
 */
public final class ServerEffectController {
    /** Duration each (re-)application grants. Long enough that the icon timer never visibly runs low. */
    public static final int DURATION_TICKS = 20 * 20;
    /** Refresh once remaining duration drops below this, comfortably before any visual fade-out. */
    public static final int REFRESH_BELOW_TICKS = 15 * 20;

    private final ServerOptIns optIns;
    private final Supplier<BlindnessConfig> config;
    /** Players we have applied an effect to; gates removal so we do not touch effects we did not add. */
    private final Set<UUID> applied = new HashSet<>();

    public ServerEffectController(ServerOptIns optIns, Supplier<BlindnessConfig> config) {
        this.optIns = optIns;
        this.config = config;
    }

    public void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            tickPlayer(player);
        }
    }

    private void tickPlayer(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (!optIns.isEnabled(id)) {
            if (applied.remove(id)) {
                clearManaged(player);
            }
            return;
        }

        BlindnessConfig cfg = config.get();
        boolean darkness = optIns.styleFor(id, cfg.resolvedStyle()) == EffectStyle.DARKNESS;
        RegistryEntry<StatusEffect> wanted = darkness ? StatusEffects.DARKNESS : StatusEffects.BLINDNESS;
        RegistryEntry<StatusEffect> other = darkness ? StatusEffects.BLINDNESS : StatusEffects.DARKNESS;

        StatusEffectInstance current = player.getStatusEffect(wanted);
        boolean satisfied = current != null
                && (current.isInfinite() || current.getDuration() >= REFRESH_BELOW_TICKS);
        if (!satisfied) {
            player.addStatusEffect(new StatusEffectInstance(
                    wanted, DURATION_TICKS, 0, false, false, cfg.showEffectIcon));
            applied.add(id);
        }
        // If the style was switched while active, drop the effect we previously applied.
        if (applied.contains(id)) {
            removeIfFinite(player, other);
        }
    }

    /** Instantly remove the managed effects from a player, e.g. right when they opt out. */
    public void clearNow(ServerPlayerEntity player) {
        applied.remove(player.getUuid());
        clearManaged(player);
    }

    /**
     * Clears leftover managed-looking effects from players who are not opted in. Run on join so a
     * player who logged out blindfolded and came back after a server restart is not stuck riding
     * out the remainder.
     */
    public void onJoin(ServerPlayerEntity player) {
        if (optIns.isEnabled(player.getUuid())) {
            return; // still opted in; the tick loop keeps the effect fresh
        }
        removeIfManagedLooking(player, StatusEffects.BLINDNESS);
        removeIfManagedLooking(player, StatusEffects.DARKNESS);
    }

    private void clearManaged(ServerPlayerEntity player) {
        removeIfFinite(player, StatusEffects.BLINDNESS);
        removeIfFinite(player, StatusEffects.DARKNESS);
    }

    /** Remove the effect unless it is infinite (an infinite instance cannot be ours). */
    private static void removeIfFinite(ServerPlayerEntity player, RegistryEntry<StatusEffect> effect) {
        StatusEffectInstance instance = player.getStatusEffect(effect);
        if (instance != null && !instance.isInfinite()) {
            player.removeStatusEffect(effect);
        }
    }

    /** Remove the effect only if it matches the exact shape this controller applies. */
    private static void removeIfManagedLooking(ServerPlayerEntity player, RegistryEntry<StatusEffect> effect) {
        StatusEffectInstance instance = player.getStatusEffect(effect);
        if (instance != null
                && !instance.isInfinite()
                && instance.getDuration() <= DURATION_TICKS
                && instance.getAmplifier() == 0
                && !instance.shouldShowParticles()) {
            player.removeStatusEffect(effect);
        }
    }
}
