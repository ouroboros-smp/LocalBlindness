package com.ouroboros.localblindness.gametest.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Gives shared CI runners enough time to finish first-run integrated-world setup.
 *
 * <p>Fabric API 4.3.5 hard-codes this wait to 1,200 ticks and does not expose a system property or
 * builder option for it. This mixin is part of the GameTest-only source set, so production clients
 * retain Fabric's default behavior.
 */
@Mixin(targets = "net.fabricmc.fabric.impl.client.gametest.util.ClientGameTestImpl", remap = false)
public abstract class ClientGameTestTimeoutMixin {
    @ModifyConstant(method = "waitForWorldLoad", constant = @Constant(intValue = 1200))
    private static int blindfoldTest$extendWorldLoadTimeout(int original) {
        return 5 * original;
    }
}
