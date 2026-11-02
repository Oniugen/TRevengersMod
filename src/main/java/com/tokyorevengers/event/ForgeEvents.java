package com.tokyorevengers.event;

import com.tokyorevengers.TokyoRevengers;
import com.tokyorevengers.command.GangCommand;
import com.tokyorevengers.gang.Reputation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import java.util.UUID;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TokyoRevengers.MOD_ID)
public class ForgeEvents {
    private static final UUID VITALITY_ID = UUID.fromString("d77e6e5c-8c72-4c25-bf93-2b9e8b87b711");
    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        GangCommand.register(event.getDispatcher());
    }

    /** Atualiza a vida máxima conforme a árvore independente de vitalidade. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        AttributeInstance maxHealth = event.player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        int level = Reputation.upgradeLevel(event.player, Reputation.Upgrade.VITALITY);
        CompoundTag data = Reputation.data(event.player);
        if (data.getInt("applied_vitality_level") != level || (level > 0 && maxHealth.getModifier(VITALITY_ID) == null)) {
            maxHealth.removeModifier(VITALITY_ID);
            if (level > 0) maxHealth.addTransientModifier(new AttributeModifier(VITALITY_ID,
                    "Tokyo Revengers vitality", level * 4.0D, AttributeModifier.Operation.ADDITION));
            data.putInt("applied_vitality_level", level);
        }
        if (event.player.getHealth() > event.player.getMaxHealth()) event.player.setHealth(event.player.getMaxHealth());
    }

    /** Mantém reputação e gangue depois de morrer. */
    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag old = event.getOriginal().getPersistentData();
        if (old.contains(Reputation.ROOT)) {
            event.getEntity().getPersistentData().put(Reputation.ROOT, old.getCompound(Reputation.ROOT).copy());
        }
    }
}
