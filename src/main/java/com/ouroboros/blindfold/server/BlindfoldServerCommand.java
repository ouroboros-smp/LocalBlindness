package com.ouroboros.blindfold.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ouroboros.blindfold.Blindfold;
import com.ouroboros.blindfold.EffectStyle;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;

/**
 * The server-side {@code /blindfold} command. Every player may use it on themselves - opting in is
 * the whole point, so no OP level is required. Only {@code reload} (which re-reads the server
 * config) is restricted to operators.
 *
 * <p>Players who also run the Blindfold client mod will have their client command intercept
 * {@code /blindfold} before it reaches the server; that is fine, because the client toggle produces
 * the same visual for them without any server involvement.
 */
public final class BlindfoldServerCommand {
    private BlindfoldServerCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                ServerOptIns optIns,
                                ServerEffectController controller) {
        dispatcher.register(CommandManager.literal(Blindfold.MOD_ID)
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    boolean on = optIns.isEnabled(player.getUuid());
                    EffectStyle style = optIns.styleFor(player.getUuid(), Blindfold.config().resolvedStyle());
                    ctx.getSource().sendFeedback(() -> Text.literal(
                            "[Blindfold] " + (on ? "ON" : "off") + " (style: " + label(style) + ")"), false);
                    return 1;
                })
                .then(CommandManager.literal("toggle").executes(ctx ->
                        setEnabled(ctx.getSource(), optIns, controller,
                                optIns.toggle(ctx.getSource().getPlayerOrThrow().getUuid()))))
                .then(CommandManager.literal("on").executes(ctx ->
                        setEnabled(ctx.getSource(), optIns, controller, true)))
                .then(CommandManager.literal("off").executes(ctx ->
                        setEnabled(ctx.getSource(), optIns, controller, false)))
                .then(CommandManager.literal("style")
                        .then(CommandManager.literal("blindness").executes(ctx ->
                                setStyle(ctx.getSource(), optIns, EffectStyle.BLINDNESS)))
                        .then(CommandManager.literal("darkness").executes(ctx ->
                                setStyle(ctx.getSource(), optIns, EffectStyle.DARKNESS))))
                .then(CommandManager.literal("reload")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {
                            BlindfoldServerCommand.reload(ctx.getSource());
                            return 1;
                        })));
    }

    private static int setEnabled(ServerCommandSource source, ServerOptIns optIns,
                                  ServerEffectController controller, boolean enabled) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        optIns.set(player.getUuid(), enabled);
        if (!enabled) {
            controller.clearNow(player); // drop the effect right away rather than next tick
        }
        source.sendFeedback(() -> Text.literal("[Blindfold] " + (enabled ? "ON" : "off")), false);
        return 1;
    }

    private static int setStyle(ServerCommandSource source, ServerOptIns optIns,
                                EffectStyle style) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        optIns.setStyle(player.getUuid(), style);
        source.sendFeedback(() -> Text.literal("[Blindfold] style set to " + label(style) + " (for you)"), false);
        return 1;
    }

    private static void reload(ServerCommandSource source) {
        EffectStyle style = Blindfold.reloadConfig().resolvedStyle();
        source.sendFeedback(() -> Text.literal(
                "[Blindfold] reloaded config (default style: " + label(style) + ")"), true);
    }

    private static String label(EffectStyle style) {
        return style.name().toLowerCase(Locale.ROOT);
    }
}
