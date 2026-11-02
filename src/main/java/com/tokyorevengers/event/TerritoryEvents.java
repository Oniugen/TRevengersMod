package com.tokyorevengers.event;

import com.tokyorevengers.TokyoRevengers;
import com.tokyorevengers.entity.GangMemberEntity;
import com.tokyorevengers.gang.Gang;
import com.tokyorevengers.gang.TerritoryData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Conquista territorial: grupos presentes disputam chunks e a maioria numérica ocupa a área. */
@Mod.EventBusSubscriber(modid = TokyoRevengers.MOD_ID)
public class TerritoryEvents {
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof GangMemberEntity member) || member.level().isClientSide
                || member.getGang().isNeutral() || member.tickCount % 200 != 0) return;
        ServerLevel level = (ServerLevel) member.level();
        AABB area = member.getBoundingBox().inflate(12.0D, 4.0D, 12.0D);
        int friendly = level.getEntitiesOfClass(GangMemberEntity.class, area,
                e -> e.isAlive() && e.getGang() == member.getGang()).size();
        if (friendly < 2) return;
        int strongestEnemy = 0;
        for (Gang rival : Gang.values()) {
            if (!Gang.areRivals(member.getGang(), rival)) continue;
            int count = level.getEntitiesOfClass(GangMemberEntity.class, area,
                    e -> e.isAlive() && e.getGang() == rival).size();
            strongestEnemy = Math.max(strongestEnemy, count);
        }
        if (friendly > strongestEnemy) {
            TerritoryData.get(level).claim(level.getChunkAt(member.blockPosition()).getPos(), member.getGang());
        }
    }
}
