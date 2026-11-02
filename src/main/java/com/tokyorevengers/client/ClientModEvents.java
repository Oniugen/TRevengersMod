package com.tokyorevengers.client;

import com.tokyorevengers.TokyoRevengers;
import com.tokyorevengers.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TokyoRevengers.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ModEntities.MEMBERS.forEach((id, type) ->
                event.registerEntityRenderer(type.get(), ctx -> new GangMemberRenderer(ctx, id)));
    }
}
