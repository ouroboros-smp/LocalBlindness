package com.ouroboros.blindfold.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ouroboros.blindfold.BlindfoldClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Drops the sprinting penalty that the Blindness effect normally imposes, but only while this mod is
 * the thing applying the effect. Only the blindness check inside the vanilla sprint gate is bypassed,
 * so the blindness fog and darkening are untouched. Real gameplay blindness is left alone whenever
 * the mod's toggle is off.
 *
 * <p>Mojang has reshaped this code across versions: the gate itself has carried different names and
 * the blindness check inside it has flipped between a dedicated helper and an inline
 * {@code hasEffect(MobEffects.BLINDNESS)} call. Both injectors below therefore use {@code require = 0}
 * and between them cover every known shape, so a future rename degrades to "vanilla sprint rules
 * while blindfolded" instead of crashing the game. The client GameTest suite asserts the actual
 * sprint behavior on every build, so a silently missed injection cannot survive CI.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @ModifyExpressionValue(
            method = {"isSprintingPossible", "canSprint"},
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasBlindness()Z"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasBlindnessEffect()Z")
            },
            require = 0
    )
    private boolean blindfold$ignoreBlindnessHelperForSprint(boolean hasBlindness) {
        if (BlindfoldClient.isEffectActive()) {
            return false; // report "no blindness" so the sprint gate stays open
        }
        return hasBlindness;
    }

    @WrapOperation(
            method = {"isSprintingPossible", "canSprint"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"),
            require = 0
    )
    private boolean blindfold$ignoreInlineBlindnessForSprint(LocalPlayer self, Holder<MobEffect> effect, Operation<Boolean> original) {
        if (effect == MobEffects.BLINDNESS && BlindfoldClient.isEffectActive()) {
            return false; // report "no blindness" so the sprint gate stays open
        }
        return original.call(self, effect);
    }
}
