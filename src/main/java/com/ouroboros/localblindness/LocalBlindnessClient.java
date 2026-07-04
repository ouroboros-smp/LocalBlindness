package com.ouroboros.localblindness;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Client entrypoint for Blindfold. Wires up:
 * <ul>
 *   <li>a toggle keybind (default 'B', rebindable in Controls),</li>
 *   <li>the {@code /blindfold} client command,</li>
 *   <li>a per-tick {@link EffectController} that applies the real Blindness/Darkness status effect
 *       to the local player and re-asserts it so server sync cannot strip it.</li>
 * </ul>
 * Everything runs on the client. The server is never contacted and never told anything.
 */
public class LocalBlindnessClient implements ClientModInitializer {
    public static final String MOD_ID = "blindfold";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Session toggle. Static so the sprint mixin can read it without a handle to the instance. */
    private static final ToggleState TOGGLE = new ToggleState();

    private BlindnessConfig config;
    private Path configPath;

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
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
        this.config = BlindnessConfig.load(configPath);

        EffectController controller = new EffectController(TOGGLE, () -> config);

        KeyBinding toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + MOD_ID + ".toggle",
                InputUtil.Type.KEYSYM,
                config.toggleKeyCode,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                boolean now = TOGGLE.toggle();
                if (client.player != null) {
                    client.player.sendMessage(status(now), true);
                }
            }
            controller.tick(client.player);
        });

        ClientCommandRegistrationCallback.EVENT.register(this::registerCommands);

        LOGGER.info("[Blindfold] ready (style={}, toggleKey={})", config.resolvedStyle(), config.toggleKeyCode);
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, Object registryAccess) {
        dispatcher.register(ClientCommandManager.literal(MOD_ID)
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(status(TOGGLE.isEnabled()));
                    return 1;
                })
                .then(ClientCommandManager.literal("toggle").executes(ctx -> {
                    ctx.getSource().sendFeedback(status(TOGGLE.toggle()));
                    return 1;
                }))
                .then(ClientCommandManager.literal("on").executes(ctx -> {
                    TOGGLE.set(true);
                    ctx.getSource().sendFeedback(status(true));
                    return 1;
                }))
                .then(ClientCommandManager.literal("off").executes(ctx -> {
                    TOGGLE.set(false);
                    ctx.getSource().sendFeedback(status(false));
                    return 1;
                }))
                .then(ClientCommandManager.literal("style")
                        .then(ClientCommandManager.literal("blindness")
                                .executes(ctx -> setStyle(ctx.getSource(), EffectStyle.BLINDNESS)))
                        .then(ClientCommandManager.literal("darkness")
                                .executes(ctx -> setStyle(ctx.getSource(), EffectStyle.DARKNESS))))
                .then(ClientCommandManager.literal("reload").executes(ctx -> {
                    this.config = BlindnessConfig.load(configPath);
                    ctx.getSource().sendFeedback(Text.literal(
                            "[Blindfold] reloaded config (style=" + styleLabel() + ")"));
                    return 1;
                })));
    }

    private int setStyle(FabricClientCommandSource source, EffectStyle style) {
        config.style = style.name();
        config.save(configPath);
        source.sendFeedback(Text.literal("[Blindfold] style set to " + style.name().toLowerCase(Locale.ROOT)));
        return 1;
    }

    private String styleLabel() {
        return config.resolvedStyle().name().toLowerCase(Locale.ROOT);
    }

    private static Text status(boolean on) {
        return Text.literal("[Blindfold] " + (on ? "ON" : "off"));
    }
}
