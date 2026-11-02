package com.tokyorevengers.registry;

import com.tokyorevengers.TokyoRevengers;
import com.tokyorevengers.entity.GangMemberEntity;
import com.tokyorevengers.gang.GangCharacter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TokyoRevengers.MOD_ID);

    /** Um tipo de entidade por personagem, indexado pelo id. */
    public static final Map<String, RegistryObject<EntityType<GangMemberEntity>>> MEMBERS = new LinkedHashMap<>();

    static {
        for (GangCharacter c : GangCharacter.ALL) {
            MEMBERS.put(c.id(), ENTITIES.register(c.id(), () ->
                    EntityType.Builder.<GangMemberEntity>of((type, level) -> new GangMemberEntity(type, level, c),
                                    MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(10)
                            .build(TokyoRevengers.MOD_ID + ":" + c.id())));
        }
    }
}
