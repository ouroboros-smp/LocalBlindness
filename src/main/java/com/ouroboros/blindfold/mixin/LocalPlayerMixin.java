package com.ouroboros.blindfold.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ouroboros.blindfold.BlindfoldClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Drops the sprinting penalty that the Blindness effect normally imposes, but only while this mod is
 * the thing applying the effect. Only the blindness check inside the vanilla sprint gating is
 * bypassed, so the blindness fog and darkening are untouched. Real gameplay blindness is left alone
 * whenever the mod's toggle is off.
 *
 * <p>Since 26.x the sprint gating is spread across several {@code LocalPlayer} predicates
 * ({@code isSprintingPossible}, {@code canStartSprinting}, {@code shouldStopRunSprinting},
 * {@code shouldStopSwimSprinting}) and the dedicated blindness helper of 1.21.x is gone, so the
 * check is an inline {@code hasEffect(MobEffects.BLINDNESS)} call whose exact home has moved between
 * versions. The injectors below therefore cover all of these entry points with {@code require = 0}:
 * the {@link WrapOperation} inspects the effect argument, so only blindness checks are ever altered,
 * and a future reshuffle degrades to "vanilla sprint rules while blindfolded" instead of crashing
 * the game. The client GameTest suite asserts the actual sprint behavior on every build, so a
 * silently missed injection cannot survive CI.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @ModifyExpressionValue(
            method = {"isSprintingPossible", "canStartSprinting", "shouldStopRunSprinting", "shouldStopSwimSprinting", "canSprint"},
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
            method = {"isSprintingPossible", "canStartSprinting", "shouldStopRunSprinting", "shouldStopSwimSprinting", "canSprint"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"),
            require = 0
    )
    private boolean blindfold$ignoreInlineBlindnessForSprint(LocalPlayer self, Holder<MobEffect> effect, Operation<Boolean> original) {
        if (effect == MobEffects.BLINDNESS && BlindfoldClient.isEffectActive()) {
            return false; // report "no blindness" so the sprint gate stays open
        }
        return original.call(self, effect);
    }

    @WrapOperation(
            method = {"isSprintingPossible", "canStartSprinting", "shouldStopRunSprinting", "shouldStopSwimSprinting", "canSprint"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z"),
            require = 0
    )
    private boolean blindfold$ignoreInheritedInlineBlindnessForSprint(LivingEntity self, Holder<MobEffect> effect, Operation<Boolean> original) {
        if (effect == MobEffects.BLINDNESS && BlindfoldClient.isEffectActive()) {
            return false; // report "no blindness" so the sprint gate stays open
        }
        return original.call(self, effect);
    }
}
