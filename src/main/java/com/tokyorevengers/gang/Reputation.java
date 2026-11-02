package com.tokyorevengers.gang;

import com.tokyorevengers.entity.GangMemberEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** Reputação por organização e progresso individual salvo no NBT do jogador. */
public final class Reputation {
    public static final String ROOT = "tokyorevengers";
    public static final int MIN = -1000, MAX = 2000;
    public static final int JOIN_REQUIREMENT = 100, HOSTILE_THRESHOLD = -50;
    public enum Upgrade { STRENGTH, RESISTANCE, VITALITY, TECHNIQUE }
    private Reputation() {}

    public static CompoundTag data(Player p) {
        CompoundTag root = p.getPersistentData();
        if (!root.contains(ROOT, Tag.TAG_COMPOUND)) root.put(ROOT, new CompoundTag());
        return root.getCompound(ROOT);
    }
    public static int get(Player p, Gang g) { return data(p).getInt("rep_" + g.id()); }
    public static void set(Player p, Gang g, int value) { data(p).putInt("rep_" + g.id(), Mth.clamp(value, MIN, MAX)); }
    public static void add(Player p, Gang g, int amount) {
        if (amount == 0 || g == Gang.NEUTRAL) return;
        set(p, g, get(p, g) + amount);
        p.displayClientMessage(Component.literal((amount > 0 ? "+" : "") + amount + " reputação • ")
                .withStyle(amount > 0 ? ChatFormatting.GREEN : ChatFormatting.RED).append(g.displayName()), true);
    }
    public static Gang getMembership(Player p) {
        String id = data(p).getString("gang");
        return id.isEmpty() ? null : Gang.byId(id);
    }
    public static void join(Player p, Gang g) { if (!g.isNeutral()) data(p).putString("gang", g.id()); }
    public static void leave(Player p) { data(p).remove("gang"); }

    public static int kills(Player p) { return data(p).getInt("total_defeats"); }
    public static int namedDefeats(Player p) { return data(p).getInt("named_defeats"); }
    public static int defeated(Player p, String id) { return data(p).getInt("defeated_" + id); }

    /** Níveis independentes: cada atributo exige uma combinação própria de vitórias. */
    public static int upgradeLevel(Player p, Upgrade upgrade) {
        int k = kills(p), n = namedDefeats(p), rep = totalPositiveReputation(p);
        return switch (upgrade) {
            case STRENGTH -> level(k, n, rep, new int[][]{{5,0,100},{20,1,300},{50,3,700}});
            case RESISTANCE -> level(k, n, rep, new int[][]{{8,0,150},{25,1,400},{55,3,800}});
            case VITALITY -> level(k, n, rep, new int[][]{{10,0,200},{30,1,500},{65,3,900}});
            case TECHNIQUE -> level(k, n, rep, new int[][]{{3,1,100},{12,3,350},{30,6,750}});
        };
    }
    private static int level(int kills, int named, int rep, int[][] req) {
        int result = 0;
        for (int i = 0; i < req.length; i++) if (kills >= req[i][0] && named >= req[i][1] && rep >= req[i][2]) result = i + 1;
        return result;
    }
    private static int totalPositiveReputation(Player p) {
        int total = 0;
        for (Gang g : Gang.values()) if (!g.isNeutral()) total += Math.max(0, get(p, g));
        return total;
    }

    public static String tier(int rep) {
        if (rep <= HOSTILE_THRESHOLD) return "Inimigo";
        if (rep < 0) return "Mal visto";
        if (rep < 100) return "Desconhecido";
        if (rep < 300) return "Conhecido";
        if (rep < 600) return "Aliado";
        return "Irmão de armas";
    }
    public static String rank(int rep) {
        if (rep < 100) return "Recruta";
        if (rep < 300) return "Membro";
        if (rep < 600) return "Capitão de tropa";
        if (rep < 1000) return "Comandante";
        return "Braço direito";
    }

    public static void onKill(ServerPlayer killer, GangMemberEntity victim) {
        Gang g = victim.getGang();
        CompoundTag d = data(killer);
        d.putInt("total_defeats", d.getInt("total_defeats") + 1);
        d.putInt("defeated_" + victim.getCharacter().id(), defeated(killer, victim.getCharacter().id()) + 1);
        if (!victim.getCharacter().id().endsWith("_member")) d.putInt("named_defeats", d.getInt("named_defeats") + 1);
        if (g == Gang.NEUTRAL) return;

        boolean leader = victim.getCharacter().leader();
        Gang own = getMembership(killer);
        if (own == g) {
            leave(killer);
            add(killer, g, -200);
            killer.sendSystemMessage(Component.literal("Você traiu sua própria gangue e foi expulso!").withStyle(ChatFormatting.DARK_RED));
            return;
        }
        add(killer, g, leader ? -100 : -30);
        for (Gang other : Gang.values()) {
            if (Gang.areRivals(other, g)) {
                int bonus = leader ? 25 : victim.getCharacter().viceLeader() ? 15 : 8;
                if (other == own) bonus *= 2;
                add(killer, other, bonus);
            }
        }
        killer.sendSystemMessage(Component.literal("Vitórias registradas: " + kills(killer)
                + " | Personagens nomeados derrotados: " + namedDefeats(killer)).withStyle(ChatFormatting.GRAY));
    }
}
