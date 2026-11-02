package com.tokyorevengers.gang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Organizações e grupos presentes no universo da mod. */
public enum Gang {
    TOMAN("toman", "Tokyo Manji (Toman)", ChatFormatting.GOLD, 0xE0A526),
    MOEBIUS("moebius", "Moebius", ChatFormatting.GRAY, 0x777777),
    VALHALLA("valhalla", "Valhalla", ChatFormatting.DARK_PURPLE, 0x5B2A86),
    BLACK_DRAGON("black_dragon", "Black Dragon", ChatFormatting.RED, 0x8B1A1A),
    TENJIKU("tenjiku", "Tenjiku", ChatFormatting.DARK_RED, 0xA51F2B),
    BRAHMAN("brahman", "Brahman", ChatFormatting.LIGHT_PURPLE, 0xD8B7C8),
    ROKUHARA("rokuhara_tandai", "Rokuhara Tandai", ChatFormatting.DARK_GREEN, 0x315D43),
    YOTSUYA_KAIDAN("yotsuya_kaidan", "Yotsuya Kaidan", ChatFormatting.BLUE, 0x315A9B),
    KIYOMASA("kiyomasa_gang", "Kiyomasa's Gang", ChatFormatting.DARK_GRAY, 0x454545),
    NEUTRAL("neutral", "Neutro", ChatFormatting.WHITE, 0xB0B0B0);

    private final String id;
    private final String displayName;
    private final ChatFormatting format;
    private final int color;

    Gang(String id, String displayName, ChatFormatting format, int color) {
        this.id = id;
        this.displayName = displayName;
        this.format = format;
        this.color = color;
    }

    public String id() { return id; }
    public int color() { return color; }
    public String plainName() { return displayName; }
    public boolean isNeutral() { return this == NEUTRAL; }

    public Component displayName() {
        return Component.literal(displayName).withStyle(format);
    }

    public static Gang byId(String id) {
        for (Gang g : values()) if (g.id.equalsIgnoreCase(id) || g.name().equalsIgnoreCase(id)) return g;
        return null;
    }

    /** Todas as organizações distintas são hostis. Neutros não iniciam guerras de gangues. */
    public static boolean areRivals(Gang a, Gang b) {
        return a != null && b != null && a != b && !a.isNeutral() && !b.isNeutral();
    }
}
