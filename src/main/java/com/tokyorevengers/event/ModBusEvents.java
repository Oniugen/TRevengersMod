package com.tokyorevengers.event;

import com.tokyorevengers.TokyoRevengers;
import com.tokyorevengers.entity.GangMemberEntity;
import com.tokyorevengers.gang.GangCharacter;
import com.tokyorevengers.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TokyoRevengers.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        for (GangCharacter c : GangCharacter.ALL) {
            event.put(ModEntities.MEMBERS.get(c.id()).get(), GangMemberEntity.createAttributes(c).build());
        }
    }
}
