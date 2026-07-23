package com.ouroboros.blindfold.mixin;

import com.ouroboros.blindfold.BlindfoldClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Drops the sprinting penalty that the Blindness effect normally imposes, but only while this mod is
 * the thing applying the effect. Only the {@code hasBlindnessEffect()} check inside {@code canSprint}
 * is bypassed, so the blindness fog and darkening are untouched. Real gameplay blindness is left alone
 * whenever the mod's toggle is off.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Redirect(
            method = "canSprint(Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasBlindnessEffect()Z"
            )
    )
    private boolean blindfold$ignoreBlindnessForSprint(ClientPlayerEntity self) {
        if (BlindfoldClient.isEffectActive()) {
            return false; // report "no blindness" so the sprint gate stays open
        }
        return self.hasBlindnessEffect();
    }
}
