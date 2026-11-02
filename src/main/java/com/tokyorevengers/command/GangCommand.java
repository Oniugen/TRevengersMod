package com.tokyorevengers.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.tokyorevengers.gang.Gang;
import com.tokyorevengers.gang.GangCharacter;
import com.tokyorevengers.gang.Reputation;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

/**
 * /gang status | list | join <gangue> | leave | setrep <jogador> <gangue> <valor> (op)
 */
public final class GangCommand {
    private static final SuggestionProvider<CommandSourceStack> GANGS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(Gang.values()).map(Gang::id), builder);

    private GangCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gang")
                .then(Commands.literal("status").executes(c -> status(c.getSource())))
                .then(Commands.literal("list").executes(c -> list(c.getSource())))
                .then(Commands.literal("leave").executes(c -> leave(c.getSource())))
                .then(Commands.literal("join")
                        .then(Commands.argument("gang", StringArgumentType.word()).suggests(GANGS)
                                .executes(c -> join(c.getSource(), StringArgumentType.getString(c, "gang")))))
                .then(Commands.literal("setrep").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("gang", StringArgumentType.word()).suggests(GANGS)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(Reputation.MIN, Reputation.MAX))
                                                .executes(c -> setRep(c.getSource(),
                                                        EntityArgument.getPlayer(c, "player"),
                                                        StringArgumentType.getString(c, "gang"),
                                                        IntegerArgumentType.getInteger(c, "value"))))))));
    }

    private static int status(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        Gang member = Reputation.getMembership(p);
        src.sendSuccess(() -> Component.literal("=== Seu status ===").withStyle(ChatFormatting.YELLOW), false);
        src.sendSuccess(() -> member == null
                ? Component.literal("Gangue: nenhuma")
                : Component.literal("Gangue: ").append(member.displayName())
                        .append(" - " + Reputation.rank(Reputation.get(p, member))), false);
        src.sendSuccess(() -> Component.literal("Vitórias: " + Reputation.kills(p)
                + " | Personagens nomeados derrotados: " + Reputation.namedDefeats(p)).withStyle(ChatFormatting.GRAY), false);
        src.sendSuccess(() -> Component.literal("Evolução independente:").withStyle(ChatFormatting.GOLD), false);
        for (Reputation.Upgrade upgrade : Reputation.Upgrade.values()) {
            int level = Reputation.upgradeLevel(p, upgrade);
            src.sendSuccess(() -> Component.literal(" • " + upgrade.name() + ": nível " + level + "/3"), false);
        }
        for (Gang g : Gang.values()) {
            if (g.isNeutral()) continue;
            int rep = Reputation.get(p, g);
            src.sendSuccess(() -> Component.literal(" • ").append(g.displayName())
                    .append(": " + rep + " (" + Reputation.tier(rep) + ")"), false);
        }
        return 1;
    }

    private static int list(CommandSourceStack src) {
        for (Gang g : Gang.values()) {
            StringBuilder sb = new StringBuilder();
            for (GangCharacter c : GangCharacter.ALL) {
                if (c.gang() == g && !c.id().endsWith("_member")) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(c.id());
                }
            }
            src.sendSuccess(() -> g.displayName().copy().append(Component.literal(": " + sb)
                    .withStyle(ChatFormatting.GRAY)), false);
        }
        return 1;
    }

    private static int join(CommandSourceStack src, String id) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        Gang g = Gang.byId(id);
        if (g == null) {
            src.sendFailure(Component.literal("Gangue desconhecida."));
            return 0;
        }
        if (g.isNeutral()) {
            src.sendFailure(Component.literal("NPCs neutros não são uma gangue recrutável."));
            return 0;
        }
        if (Reputation.getMembership(p) != null) {
            src.sendFailure(Component.literal("Você já pertence a uma gangue. Use /gang leave primeiro."));
            return 0;
        }
        int rep = Reputation.get(p, g);
        if (rep < Reputation.JOIN_REQUIREMENT && !src.hasPermission(2)) {
            src.sendFailure(Component.literal("Reputação insuficiente: " + rep + "/" + Reputation.JOIN_REQUIREMENT));
            return 0;
        }
        Reputation.join(p, g);
        src.sendSuccess(() -> Component.literal("Você entrou na ").append(g.displayName()).append("!"), false);
        return 1;
    }

    private static int leave(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        Gang g = Reputation.getMembership(p);
        if (g == null) {
            src.sendFailure(Component.literal("Você não está em nenhuma gangue."));
            return 0;
        }
        Reputation.leave(p);
        Reputation.add(p, g, -30);
        src.sendSuccess(() -> Component.literal("Você saiu da ").append(g.displayName()).append("."), false);
        return 1;
    }

    private static int setRep(CommandSourceStack src, ServerPlayer target, String id, int value) {
        Gang g = Gang.byId(id);
        if (g == null) {
            src.sendFailure(Component.literal("Gangue desconhecida."));
            return 0;
        }
        Reputation.set(target, g, value);
        src.sendSuccess(() -> Component.literal("Reputação de " + target.getName().getString() + " com ")
                .append(g.displayName()).append(" = " + value), true);
        return 1;
    }
}
