package com.tokyorevengers.event;

import com.tokyorevengers.TokyoRevengers;
import com.tokyorevengers.gang.Gang;
import com.tokyorevengers.gang.Reputation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Combate do jogador: quem pertence a uma gangue ganha
 *  - COMBO: golpes seguidos (até 2s entre eles) aumentam o dano em até +40%
 *  - VOADORA: ataque de mão vazia correndo e no ar causa +4 de dano e empurra o alvo
 */
@Mod.EventBusSubscriber(modid = TokyoRevengers.MOD_ID)
public class CombatEvents {
    private static final int COMBO_WINDOW_TICKS = 40;
    private static final int KICK_COOLDOWN_TICKS = 60;

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().getDirectEntity() != player) return; // só corpo a corpo

        LivingEntity target = event.getEntity();
        long now = player.level().getGameTime();
        CompoundTag data = Reputation.data(player);
        float amount = event.getAmount();
        amount += Reputation.upgradeLevel(player, Reputation.Upgrade.STRENGTH) * 2.0F;

        // ----- Combo -----
        int combo = (now - data.getLong("combo_time") <= COMBO_WINDOW_TICKS) ? data.getInt("combo") + 1 : 1;
        data.putInt("combo", combo);
        data.putLong("combo_time", now);
        if (combo >= 2) {
            int technique = Reputation.upgradeLevel(player, Reputation.Upgrade.TECHNIQUE);
            amount *= 1.0F + Math.min(combo - 1, 5) * (0.08F + technique * 0.02F);
            if (combo >= 3) {
                player.displayClientMessage(Component.literal("Combo x" + combo + "!")
                        .withStyle(ChatFormatting.GOLD), true);
            }
        }

        // ----- Voadora -----
        if (player.isSprinting() && !player.onGround() && player.getMainHandItem().isEmpty()
                && now - data.getLong("kick_time") >= KICK_COOLDOWN_TICKS) {
            data.putLong("kick_time", now);
            amount += 4.0F;
            float yaw = player.getYRot() * ((float) Math.PI / 180F);
            target.knockback(0.9D, Mth.sin(yaw), -Mth.cos(yaw));
            player.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5D), target.getZ(),
                        15, 0.3D, 0.3D, 0.3D, 0.15D);
            }
            player.displayClientMessage(Component.literal("Voadora!").withStyle(ChatFormatting.RED), true);
        }

        event.setAmount(amount);
    }

    /** Resistência reduz dano recebido; a melhoria é independente da força. */
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int level = Reputation.upgradeLevel(player, Reputation.Upgrade.RESISTANCE);
        if (level > 0) event.setAmount(event.getAmount() * (1.0F - 0.05F * level));
    }
}
