package com.ouroboros.blindfold;

import com.ouroboros.blindfold.server.BlindfoldServerCommand;
import com.ouroboros.blindfold.server.ServerEffectController;
import com.ouroboros.blindfold.server.ServerOptIns;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Common entrypoint for Blindfold, run on both the client and the dedicated server. Wires up the
 * server-side opt-in flow:
 * <ul>
 *   <li>the {@code /blindfold} server command, usable by every player (no OP needed) to opt in or
 *       out of the effect for themselves,</li>
 *   <li>a per-server-tick {@link ServerEffectController} that keeps a short, self-refreshing
 *       Blindness/Darkness effect on every opted-in player,</li>
 *   <li>a join hook that clears any leftover managed effect for players who are not opted in
 *       (for example after a server crash).</li>
 * </ul>
 * The client-only half of the mod (keybind, local toggle) lives in {@link BlindfoldClient} and is
 * untouched by any of this. Both halves share the config loaded here.
 */
public class Blindfold implements ModInitializer {
    public static final String MOD_ID = "blindfold";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static volatile BlindnessConfig config;
    private static Path configPath;

    /** The shared config instance. Never null after the mod initializes. */
    public static BlindnessConfig config() {
        return config;
    }

    /** Re-read the config file from disk, replacing the shared instance. */
    public static BlindnessConfig reloadConfig() {
        config = BlindnessConfig.load(configPath);
        return config;
    }

    /** Persist the shared config instance to disk. */
    public static void saveConfig() {
        config.save(configPath);
    }

    @Override
    public void onInitialize() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
        config = BlindnessConfig.load(configPath);

        ServerOptIns optIns = new ServerOptIns();
        ServerEffectController controller = new ServerEffectController(optIns, Blindfold::config);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                BlindfoldServerCommand.register(dispatcher, optIns, controller));

        ServerTickEvents.END_SERVER_TICK.register(controller::tick);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                controller.onJoin(handler.player));

        LOGGER.info("[Blindfold] ready (default style={})", config.resolvedStyle());
    }
}
