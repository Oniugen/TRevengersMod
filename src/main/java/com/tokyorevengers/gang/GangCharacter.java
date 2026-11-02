package com.tokyorevengers.gang;

import java.util.List;

/** Ficha individual de um personagem. Os atributos são valores-base do Minecraft (HP/dano/velocidade). */
public record GangCharacter(String id, Gang gang, boolean leader, boolean viceLeader,
                            double health, double damage, double speed,
                            Ability ability, String quote) {

    public enum Ability { NONE, KICK, COMBO, GUARD, BERSERK, TENACITY, RALLY }

    private static GangCharacter c(String id, Gang g, boolean leader, boolean vice, double hp, double dmg,
                                   double spd, Ability a, String quote) {
        return new GangCharacter(id, g, leader, vice, hp, dmg, spd, a, quote);
    }
    private static GangCharacter c(String id, Gang g, double hp, double dmg, double spd, Ability a) {
        return c(id, g, false, false, hp, dmg, spd, a, "Não tenho nada para conversar com você.");
    }

    public static final List<GangCharacter> ALL = List.of(
        // Tokyo Manji Gang
        c("mikey", Gang.TOMAN, true, false, 160, 30, .38, Ability.KICK, "Se quiser andar com a Toman, mostre que tem coragem."),
        c("draken", Gang.TOMAN, false, true, 150, 23, .34, Ability.BERSERK, "O Mikey é o nosso líder. Respeite isso."),
        c("mitsuya", Gang.TOMAN, 110, 15, .32, Ability.COMBO),
        c("baji", Gang.TOMAN, 130, 17, .36, Ability.BERSERK),
        c("chifuyu", Gang.TOMAN, 90, 11, .34, Ability.COMBO),
        c("takemichi", Gang.TOMAN, 120, 8, .29, Ability.TENACITY),
        c("pah_chin", Gang.TOMAN, 110, 14, .28, Ability.GUARD),
        c("peh_yan", Gang.TOMAN, 115, 16, .31, Ability.BERSERK),
        c("hakkai", Gang.TOMAN, 125, 16, .29, Ability.GUARD),
        c("muto_toman", Gang.TOMAN, 130, 17, .30, Ability.GUARD),
        c("haruchiyo_toman", Gang.TOMAN, 110, 16, .36, Ability.COMBO),
        c("nahoya", Gang.TOMAN, 115, 16, .35, Ability.BERSERK),
        c("souya", Gang.TOMAN, 115, 18, .34, Ability.BERSERK),
        c("ryusei_satou", Gang.TOMAN, 100, 13, .33, Ability.COMBO),
        c("toman_member", Gang.TOMAN, 55, 7, .30, Ability.NONE),

        // Moebius
        c("osanai", Gang.MOEBIUS, true, false, 125, 18, .31, Ability.BERSERK, "Você está no território da Moebius."),
        c("moebius_member", Gang.MOEBIUS, 52, 7, .30, Ability.NONE),

        // Valhalla
        c("kisaki", Gang.VALHALLA, true, false, 65, 7, .29, Ability.RALLY, "Cada peça tem seu lugar no tabuleiro."),
        c("hanma_valhalla", Gang.VALHALLA, false, true, 145, 19, .36, Ability.TENACITY, "Hehe... finalmente algo divertido apareceu."),
        c("kazutora", Gang.VALHALLA, 115, 16, .35, Ability.BERSERK),
        c("chome", Gang.VALHALLA, 90, 11, .30, Ability.NONE),
        c("chonbo", Gang.VALHALLA, 95, 12, .29, Ability.GUARD),
        c("choji", Gang.VALHALLA, 90, 11, .31, Ability.NONE),
        c("tadaomi", Gang.VALHALLA, 90, 11, .31, Ability.NONE),
        c("valhalla_member", Gang.VALHALLA, 55, 7, .30, Ability.NONE),

        // Black Dragon
        c("taiju", Gang.BLACK_DRAGON, true, false, 180, 25, .30, Ability.BERSERK, "Nos da Black Dragons, só respeitamos quem sabe lutar."),
        c("inui_black_dragon", Gang.BLACK_DRAGON, 105, 14, .33, Ability.COMBO),
        c("koko_black_dragon", Gang.BLACK_DRAGON, 85, 10, .32, Ability.RALLY),
        c("black_dragon_member", Gang.BLACK_DRAGON, 60, 8, .30, Ability.NONE),

        // Tenjiku (versões de época independentes)
        c("izana", Gang.TENJIKU, true, false, 165, 24, .39, Ability.KICK, "Na Tenjiku, quem manda sou eu."),
        c("kakucho_tenjiku", Gang.TENJIKU, false, true, 155, 22, .35, Ability.GUARD, "Se for ficar do nosso lado, que seja de verdade."),
        c("ran_tenjiku", Gang.TENJIKU, 125, 17, .34, Ability.COMBO),
        c("rindo_tenjiku", Gang.TENJIKU, 120, 17, .35, Ability.COMBO),
        c("mochizuki_tenjiku", Gang.TENJIKU, 160, 22, .29, Ability.BERSERK),
        c("shion_tenjiku", Gang.TENJIKU, 85, 10, .30, Ability.NONE),
        c("muto_tenjiku", Gang.TENJIKU, 130, 17, .30, Ability.GUARD),
        c("hanma_tenjiku", Gang.TENJIKU, 145, 19, .36, Ability.TENACITY),
        c("koko_tenjiku", Gang.TENJIKU, 85, 10, .32, Ability.RALLY),
        c("haruchiyo_tenjiku", Gang.TENJIKU, 110, 16, .36, Ability.COMBO),
        c("tenjiku_member", Gang.TENJIKU, 65, 9, .31, Ability.NONE),

        // Brahman
        c("senju", Gang.BRAHMAN, true, false, 125, 20, .41, Ability.KICK, "Não subestime a Brahman."),
        c("takeomi", Gang.BRAHMAN, false, true, 105, 13, .32, Ability.RALLY, "Pense bem antes de agir."),
        c("benkei", Gang.BRAHMAN, 165, 23, .29, Ability.GUARD),
        c("wakasa", Gang.BRAHMAN, 130, 21, .42, Ability.KICK),
        c("brahman_member", Gang.BRAHMAN, 65, 9, .31, Ability.NONE),

        // Rokuhara Tandai
        c("south", Gang.ROKUHARA, true, false, 200, 29, .34, Ability.BERSERK, "Eu sou o mais forte."),
        c("kakucho_rokuhara", Gang.ROKUHARA, false, true, 155, 22, .35, Ability.GUARD, "Não vou recuar."),
        c("ran_rokuhara", Gang.ROKUHARA, 125, 17, .34, Ability.COMBO),
        c("rindo_rokuhara", Gang.ROKUHARA, 120, 17, .35, Ability.COMBO),
        c("mochizuki_rokuhara", Gang.ROKUHARA, 160, 22, .29, Ability.BERSERK),
        c("shion_rokuhara", Gang.ROKUHARA, 85, 10, .30, Ability.NONE),
        c("rokuhara_member", Gang.ROKUHARA, 65, 9, .31, Ability.NONE),

        // Yotsuya Kaidan
        c("kensho_hishi", Gang.YOTSUYA_KAIDAN, true, false, 120, 17, .33, Ability.COMBO, "Yotsuya Kaidan não aceita covardes."),
        c("ayumu_tobishima", Gang.YOTSUYA_KAIDAN, 100, 13, .32, Ability.NONE),
        c("soichiro_matsuno", Gang.YOTSUYA_KAIDAN, 100, 13, .31, Ability.COMBO),
        c("kiyomaro_mori", Gang.YOTSUYA_KAIDAN, 105, 14, .30, Ability.GUARD),
        c("kazuya_atsushima", Gang.YOTSUYA_KAIDAN, 100, 13, .32, Ability.NONE),
        c("yotsuya_member", Gang.YOTSUYA_KAIDAN, 55, 7, .30, Ability.NONE),

        // Grupo paralelo de Kiyomasa
        c("kiyomasa", Gang.KIYOMASA, true, false, 75, 10, .30, Ability.NONE, "Você sabe quem manda por aqui?"),
        c("kiyomasa_member", Gang.KIYOMASA, 42, 6, .29, Ability.NONE),

        // Neutros e aposentados
        c("kojiro_hishi", Gang.NEUTRAL, 120, 18, .34, Ability.BERSERK),
        c("draken_retired", Gang.NEUTRAL, 145, 19, .32, Ability.GUARD),
        c("inui_retired", Gang.NEUTRAL, 105, 14, .33, Ability.COMBO)
    );

    public static GangCharacter byId(String id) {
        for (GangCharacter c : ALL) if (c.id.equals(id)) return c;
        return null;
    }
}
