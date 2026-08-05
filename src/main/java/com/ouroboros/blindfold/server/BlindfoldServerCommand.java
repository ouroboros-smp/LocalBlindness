package com.ouroboros.blindfold.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.ouroboros.blindfold.Blindfold;
import com.ouroboros.blindfold.EffectStyle;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                ServerOptIns optIns,
                                ServerEffectController controller) {
        dispatcher.register(Commands.literal(Blindfold.MOD_ID)
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    boolean on = optIns.isEnabled(player.getUUID());
                    EffectStyle style = optIns.styleFor(player.getUUID(), Blindfold.config().resolvedStyle());
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "[Blindfold] " + (on ? "ON" : "off") + " (style: " + label(style) + ")"), false);
                    return 1;
                })
                .then(Commands.literal("toggle").executes(ctx ->
                        setEnabled(ctx.getSource(), optIns, controller,
                                optIns.toggle(ctx.getSource().getPlayerOrException().getUUID()))))
                .then(Commands.literal("on").executes(ctx ->
                        setEnabled(ctx.getSource(), optIns, controller, true)))
                .then(Commands.literal("off").executes(ctx ->
                        setEnabled(ctx.getSource(), optIns, controller, false)))
                .then(Commands.literal("style")
                        .then(Commands.literal("blindness").executes(ctx ->
                                setStyle(ctx.getSource(), optIns, EffectStyle.BLINDNESS)))
                        .then(Commands.literal("darkness").executes(ctx ->
                                setStyle(ctx.getSource(), optIns, EffectStyle.DARKNESS))))
                .then(Commands.literal("reload")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> {
                            BlindfoldServerCommand.reload(ctx.getSource());
                            return 1;
                        })));
    }

    private static int setEnabled(CommandSourceStack source, ServerOptIns optIns,
                                  ServerEffectController controller, boolean enabled) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        optIns.set(player.getUUID(), enabled);
        if (!enabled) {
            controller.clearNow(player); // drop the effect right away rather than next tick
        }
        source.sendSuccess(() -> Component.literal("[Blindfold] " + (enabled ? "ON" : "off")), false);
        return 1;
    }

    private static int setStyle(CommandSourceStack source, ServerOptIns optIns,
                                EffectStyle style) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        optIns.setStyle(player.getUUID(), style);
        source.sendSuccess(() -> Component.literal("[Blindfold] style set to " + label(style) + " (for you)"), false);
        return 1;
    }

    private static void reload(CommandSourceStack source) {
        EffectStyle style = Blindfold.reloadConfig().resolvedStyle();
        source.sendSuccess(() -> Component.literal(
                "[Blindfold] reloaded config (default style: " + label(style) + ")"), true);
    }

    private static String label(EffectStyle style) {
        return style.name().toLowerCase(Locale.ROOT);
    }
}
