package com.ouroboros.blindfold.gametest;

import com.ouroboros.blindfold.BlindfoldClient;
import java.lang.reflect.Method;
import java.util.Properties;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

@SuppressWarnings("UnstableApiUsage")
public final class BlindfoldClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        // The client gametest API logs world state every wait-loop iteration. GitHub's captured
        // console can otherwise starve the integrated server long enough to hit Fabric's fixed
        // startup timeout.
        Configurator.setLevel("fabric-client-gametest-api-v1", Level.WARN);
        context.runOnClient(client -> {
            // Keep world startup comfortably inside Fabric's fixed world-load timeout on shared CI runners.
            client.options.renderDistance().set(2);
            client.options.simulationDistance().set(5);
            client.options.framerateLimit().set(60);
            client.options.enableVsync().set(false);
        });
        Properties serverProperties = new Properties();
        serverProperties.setProperty("view-distance", "2");
        serverProperties.setProperty("simulation-distance", "5");
        serverProperties.setProperty("generate-structures", "false");
        serverProperties.setProperty("spawn-animals", "false");
        serverProperties.setProperty("spawn-npcs", "false");
        try (TestDedicatedServerContext server = context.worldBuilder().createServer(serverProperties);
             TestDedicatedServerConnection connection = server.connect()) {
            connection.waitForChunksDownload();
            context.waitFor(client -> client.player != null);

            command(context, "blindfold off");
            command(context, "blindfold style blindness");
            context.waitFor(client -> !BlindfoldClient.isEffectActive());

            context.runOnClient(client -> {
                LocalPlayer player = requirePlayer(client.player);
                player.getFoodData().setFoodLevel(20);
                player.removeEffect(MobEffects.BLINDNESS);
                player.removeEffect(MobEffects.DARKNESS);
                check(canSprint(player), "healthy player should satisfy the vanilla sprint predicate");
            });

            command(context, "blindfold on");
            context.waitFor(client -> client.player != null
                    && BlindfoldClient.isEffectActive()
                    && client.player.hasEffect(MobEffects.BLINDNESS));
            context.runOnClient(client -> {
                LocalPlayer player = requirePlayer(client.player);
                MobEffectInstance effect = player.getEffect(MobEffects.BLINDNESS);
                check(effect != null && effect.isInfiniteDuration(), "Blindfold must apply infinite vanilla Blindness");
                check(!player.hasEffect(MobEffects.DARKNESS), "Blindness style must not leave Darkness active");
                check(canSprint(player), "Blindfold's own Blindness must not block sprinting");
            });
            context.takeScreenshot("blindfold-blindness-active");

            context.runOnClient(client -> requirePlayer(client.player).removeEffect(MobEffects.BLINDNESS));
            context.waitFor(client -> client.player != null
                    && client.player.hasEffect(MobEffects.BLINDNESS));

            command(context, "blindfold style darkness");
            context.waitFor(client -> client.player != null
                    && client.player.hasEffect(MobEffects.DARKNESS)
                    && !client.player.hasEffect(MobEffects.BLINDNESS));
            context.runOnClient(client -> {
                MobEffectInstance effect = requirePlayer(client.player).getEffect(MobEffects.DARKNESS);
                check(effect != null && effect.isInfiniteDuration(), "style switch must apply infinite vanilla Darkness");
            });
            context.takeScreenshot("blindfold-darkness-active");

            command(context, "blindfold off");
            context.waitFor(client -> client.player != null
                    && !BlindfoldClient.isEffectActive()
                    && !client.player.hasEffect(MobEffects.BLINDNESS)
                    && !client.player.hasEffect(MobEffects.DARKNESS));

            context.runOnClient(client -> requirePlayer(client.player).addEffect(
                    new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false)));
            context.waitTicks(2);
            context.runOnClient(client -> {
                LocalPlayer player = requirePlayer(client.player);
                check(player.hasEffect(MobEffects.BLINDNESS),
                        "Blindfold must not remove gameplay Blindness while toggled off");
                check(!canSprint(player), "gameplay Blindness must retain vanilla sprint blocking while Blindfold is off");
                player.removeEffect(MobEffects.BLINDNESS);
            });

            command(context, "blindfold style blindness");
        }
    }

    private static void command(ClientGameTestContext context, String command) {
        context.runOnClient(client -> requirePlayer(client.player).connection.sendCommand(command));
        context.waitTick();
    }

    /**
     * Invokes the exact sprint predicate wrapped by Blindfold's production mixin. Resolved
     * reflectively (boolean parameters filled with {@code false}, matching the old
     * {@code canSprint(false)} call) so the test fails with a clear message if Mojang renames it
     * again, instead of silently testing nothing.
     */
    private static boolean canSprint(LocalPlayer player) {
        Method predicate = null;
        for (String name : new String[] {"isSprintingPossible", "canSprint"}) {
            for (Method method : LocalPlayer.class.getDeclaredMethods()) {
                if (method.getName().equals(name) && method.getReturnType() == boolean.class) {
                    predicate = method;
                    break;
                }
            }
            if (predicate != null) {
                break;
            }
        }
        if (predicate == null) {
            throw new AssertionError("LocalPlayer sprint predicate (isSprintingPossible) not found");
        }
        Object[] args = new Object[predicate.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            if (predicate.getParameterTypes()[i] != boolean.class) {
                throw new AssertionError("unexpected sprint predicate signature: " + predicate);
            }
            args[i] = false;
        }
        try {
            predicate.setAccessible(true);
            return (boolean) predicate.invoke(player, args);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("failed to invoke sprint predicate " + predicate, e);
        }
    }

    private static LocalPlayer requirePlayer(LocalPlayer player) {
        if (player == null) throw new AssertionError("client player is unavailable");
        return player;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
