package com.ouroboros.localblindness.gametest;

import com.ouroboros.localblindness.LocalBlindnessClient;
import com.ouroboros.localblindness.gametest.mixin.ClientPlayerEntityInvoker;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

@SuppressWarnings("UnstableApiUsage")
public final class BlindfoldClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getClientWorld().waitForChunksDownload();
            context.waitFor(client -> client.player != null);

            command(context, "blindfold off");
            command(context, "blindfold style blindness");
            context.waitFor(client -> !LocalBlindnessClient.isEffectActive());

            context.runOnClient(client -> {
                ClientPlayerEntity player = requirePlayer(client.player);
                player.getHungerManager().setFoodLevel(20);
                player.removeStatusEffect(StatusEffects.BLINDNESS);
                player.removeStatusEffect(StatusEffects.DARKNESS);
                check(canSprint(player), "healthy player should satisfy the vanilla sprint predicate");
            });

            command(context, "blindfold on");
            context.waitFor(client -> client.player != null
                    && LocalBlindnessClient.isEffectActive()
                    && client.player.hasStatusEffect(StatusEffects.BLINDNESS));
            context.runOnClient(client -> {
                ClientPlayerEntity player = requirePlayer(client.player);
                StatusEffectInstance effect = player.getStatusEffect(StatusEffects.BLINDNESS);
                check(effect != null && effect.isInfinite(), "Blindfold must apply infinite vanilla Blindness");
                check(!player.hasStatusEffect(StatusEffects.DARKNESS), "Blindness style must not leave Darkness active");
                check(canSprint(player), "Blindfold's own Blindness must not block sprinting");
            });
            context.takeScreenshot("blindfold-blindness-active");

            context.runOnClient(client -> requirePlayer(client.player).removeStatusEffect(StatusEffects.BLINDNESS));
            context.waitFor(client -> client.player != null
                    && client.player.hasStatusEffect(StatusEffects.BLINDNESS));

            command(context, "blindfold style darkness");
            context.waitFor(client -> client.player != null
                    && client.player.hasStatusEffect(StatusEffects.DARKNESS)
                    && !client.player.hasStatusEffect(StatusEffects.BLINDNESS));
            context.runOnClient(client -> {
                StatusEffectInstance effect = requirePlayer(client.player).getStatusEffect(StatusEffects.DARKNESS);
                check(effect != null && effect.isInfinite(), "style switch must apply infinite vanilla Darkness");
            });
            context.takeScreenshot("blindfold-darkness-active");

            command(context, "blindfold off");
            context.waitFor(client -> client.player != null
                    && !LocalBlindnessClient.isEffectActive()
                    && !client.player.hasStatusEffect(StatusEffects.BLINDNESS)
                    && !client.player.hasStatusEffect(StatusEffects.DARKNESS));

            context.runOnClient(client -> requirePlayer(client.player).addStatusEffect(
                    new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0, false, false)));
            context.waitTicks(2);
            context.runOnClient(client -> {
                ClientPlayerEntity player = requirePlayer(client.player);
                check(player.hasStatusEffect(StatusEffects.BLINDNESS),
                        "Blindfold must not remove gameplay Blindness while toggled off");
                check(!canSprint(player), "gameplay Blindness must retain vanilla sprint blocking while Blindfold is off");
                player.removeStatusEffect(StatusEffects.BLINDNESS);
            });

            command(context, "blindfold style blindness");
        }
    }

    private static void command(ClientGameTestContext context, String command) {
        context.runOnClient(client -> requirePlayer(client.player).networkHandler.sendChatCommand(command));
        context.waitTick();
    }

    private static boolean canSprint(ClientPlayerEntity player) {
        return ((ClientPlayerEntityInvoker) player).blindfoldTest$canSprint(false);
    }

    private static ClientPlayerEntity requirePlayer(ClientPlayerEntity player) {
        if (player == null) throw new AssertionError("client player is unavailable");
        return player;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
