package com.ouroboros.blindfold.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ouroboros.blindfold.BlindfoldClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Drops the sprinting penalty that the Blindness effect normally imposes, but only while this mod is
 * the thing applying the effect. In 26.2 the sprint gates ({@code canStartSprinting},
 * {@code shouldStopRunSprinting}, {@code shouldStopSwimSprinting}) all funnel through
 * {@code isSprintingPossible}, whose blindness check is generalized into a single
 * {@code isMobilityRestricted()} call - so that call is wrapped and reported as unrestricted while
 * Blindfold's own effect is active. Only sprint gating is affected; the blindness fog and darkening
 * are untouched, and real gameplay blindness keeps its vanilla penalty whenever the toggle is off.
 *
 * <p>{@code require = 0}: if a future update reshapes the gate again, the mod degrades to "vanilla
 * sprint rules while blindfolded" instead of crashing the game. The client GameTest suite asserts
 * the actual sprint behavior in both directions on every build, so a silently missed injection
 * cannot survive CI.
 *
 * <p>Known approximation: while the toggle is on, any <em>other</em> mobility-restricting condition
 * folded into {@code isMobilityRestricted()} is bypassed too. Vanilla's only such condition is
 * blindness (which is indistinguishable from ours anyway - same effect type), matching what the
 * pre-26.2 redirect did.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @WrapOperation(
            method = "isSprintingPossible",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isMobilityRestricted()Z"),
            require = 0
    )
    private boolean blindfold$ignoreOwnBlindnessMobilityRestriction(LocalPlayer self, Operation<Boolean> original) {
        if (BlindfoldClient.isEffectActive()) {
            return false; // report "mobility unrestricted" so the sprint gate stays open
        }
        return original.call(self);
    }
}
