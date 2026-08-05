package com.ouroboros.blindfold;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Client entrypoint for Blindfold. Wires up:
 * <ul>
 *   <li>a toggle keybind (default 'B', rebindable in Controls),</li>
 *   <li>the {@code /blindfold} client command,</li>
 *   <li>a per-tick {@link EffectController} that applies the real Blindness/Darkness status effect
 *       to the local player and re-asserts it so server sync cannot strip it.</li>
 * </ul>
 * Everything here runs on the client. The server is never contacted and never told anything. (The
 * separate, opt-in server side of the mod is wired up in {@link Blindfold}; the two halves share
 * only the config, which the common entrypoint loads first.)
 */
public class BlindfoldClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Blindfold.MOD_ID);

    /** Session toggle. Static so the sprint mixin can read it without a handle to the instance. */
    private static final ToggleState TOGGLE = new ToggleState();

    /**
     * True while the mod is actively blinding the player. Read by the sprint mixin so the vanilla
     * blindness sprint/swim penalty is dropped only while our effect is on (real gameplay blindness is
     * left alone when the toggle is off).
     */
    public static boolean isEffectActive() {
        return TOGGLE.isEnabled();
    }

    @Override
    public void onInitializeClient() {
        BlindnessConfig config = Blindfold.config();

        EffectController controller = new EffectController(TOGGLE, Blindfold::config);

        KeyMapping toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key." + Blindfold.MOD_ID + ".toggle",
                InputConstants.Type.KEYSYM,
                config.toggleKeyCode,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                boolean now = TOGGLE.toggle();
                if (client.player != null) {
                    client.gui.hud.setOverlayMessage(status(now), false);
                }
            }
            controller.tick(client.player);
        });

        ClientCommandRegistrationCallback.EVENT.register(this::registerCommands);

        LOGGER.info("[Blindfold] client ready (style={}, toggleKey={})", config.resolvedStyle(), config.toggleKeyCode);
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(ClientCommands.literal(Blindfold.MOD_ID)
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(status(TOGGLE.isEnabled()));
                    return 1;
                })
                .then(ClientCommands.literal("toggle").executes(ctx -> {
                    ctx.getSource().sendFeedback(status(TOGGLE.toggle()));
                    return 1;
                }))
                .then(ClientCommands.literal("on").executes(ctx -> {
                    TOGGLE.set(true);
                    ctx.getSource().sendFeedback(status(true));
                    return 1;
                }))
                .then(ClientCommands.literal("off").executes(ctx -> {
                    TOGGLE.set(false);
                    ctx.getSource().sendFeedback(status(false));
                    return 1;
                }))
                .then(ClientCommands.literal("style")
                        .then(ClientCommands.literal("blindness")
                                .executes(ctx -> setStyle(ctx.getSource(), EffectStyle.BLINDNESS)))
                        .then(ClientCommands.literal("darkness")
                                .executes(ctx -> setStyle(ctx.getSource(), EffectStyle.DARKNESS))))
                .then(ClientCommands.literal("reload").executes(ctx -> {
                    BlindnessConfig reloaded = Blindfold.reloadConfig();
                    ctx.getSource().sendFeedback(Component.literal(
                            "[Blindfold] reloaded config (style=" + styleLabel(reloaded) + ")"));
                    return 1;
                })));
    }

    private int setStyle(FabricClientCommandSource source, EffectStyle style) {
        Blindfold.config().style = style.name();
        Blindfold.saveConfig();
        source.sendFeedback(Component.literal("[Blindfold] style set to " + style.name().toLowerCase(Locale.ROOT)));
        return 1;
    }

    private static String styleLabel(BlindnessConfig config) {
        return config.resolvedStyle().name().toLowerCase(Locale.ROOT);
    }

    private static Component status(boolean on) {
        return Component.literal("[Blindfold] " + (on ? "ON" : "off"));
    }
}
