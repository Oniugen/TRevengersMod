package com.tokyorevengers;

import com.tokyorevengers.registry.ModEntities;
import com.tokyorevengers.registry.ModItems;
import com.tokyorevengers.registry.ModTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TokyoRevengers.MOD_ID)
public class TokyoRevengers {
    public static final String MOD_ID = "tokyorevengers";

    public TokyoRevengers() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        ModTabs.TABS.register(bus);
    }
}
