package com.ouroboros.blindfold.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.ouroboros.blindfold.BlindfoldClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Drops the sprinting penalty that the Blindness effect normally imposes, but only while this mod is
 * the thing applying the effect. Only the blindness check inside {@code isSprintingPossible} is
 * bypassed, so the blindness fog and darkening are untouched. Real gameplay blindness is left alone
 * whenever the mod's toggle is off.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @ModifyExpressionValue(
            method = "isSprintingPossible",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;hasBlindness()Z"
            )
    )
    private boolean blindfold$ignoreBlindnessForSprint(boolean hasBlindness) {
        if (BlindfoldClient.isEffectActive()) {
            return false; // report "no blindness" so the sprint gate stays open
        }
        return hasBlindness;
    }
}
