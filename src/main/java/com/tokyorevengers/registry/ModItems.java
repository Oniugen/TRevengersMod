package com.tokyorevengers.registry;

import com.tokyorevengers.TokyoRevengers;
import com.tokyorevengers.gang.GangCharacter;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TokyoRevengers.MOD_ID);

    /** Presente para ganhar reputação com membros de gangues. */
    public static final RegistryObject<Item> TAIYAKI = ITEMS.register("taiyaki",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(5).saturationMod(0.6F).build())));

    public static final RegistryObject<Item> IRON_PIPE = ITEMS.register("iron_pipe",
            () -> new SwordItem(Tiers.IRON, 2, -2.4F, new Item.Properties()));

    /** Faca usada pela IA de Kiyomasa; item de combate fictício da mod. */
    public static final RegistryObject<Item> KNIFE = ITEMS.register("knife",
            () -> new SwordItem(Tiers.STONE, 1, -2.2F, new Item.Properties()));

    static {
        for (GangCharacter c : GangCharacter.ALL) {
            ITEMS.register(c.id() + "_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.MEMBERS.get(c.id()), c.gang().color(), 0xFFFFFF,
                            new Item.Properties()));
        }
    }
}
