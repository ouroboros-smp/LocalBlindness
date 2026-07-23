package com.ouroboros.blindfold.gametest.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Test-only access to the exact sprint predicate wrapped by Blindfold's production mixin. */
@Mixin(ClientPlayerEntity.class)
public interface ClientPlayerEntityInvoker {
    @Invoker("canSprint")
    boolean blindfoldTest$canSprint(boolean allowTouchingWater);
}
