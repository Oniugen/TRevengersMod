package com.tokyorevengers.client;

import com.tokyorevengers.TokyoRevengers;
import com.tokyorevengers.entity.GangMemberEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/** Renderiza o personagem com o modelo do jogador (Steve) e uma skin 64x64 própria. */
public class GangMemberRenderer extends HumanoidMobRenderer<GangMemberEntity, PlayerModel<GangMemberEntity>> {
    private final ResourceLocation texture;

    public GangMemberRenderer(EntityRendererProvider.Context ctx, String id) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                ctx.getModelManager()));
        String skinId = switch (id) {
            case "pah_chin" -> "pahchin";
            case "peh_yan" -> "peyan";
            case "haruchiyo_toman", "haruchiyo_tenjiku" -> "sanzu";
            case "inui_black_dragon" -> "inui";
            case "koko_black_dragon", "koko_tenjiku" -> "koko";
            case "hanma_valhalla", "hanma_tenjiku" -> "hanma";
            case "kakucho_tenjiku", "kakucho_rokuhara" -> "kakucho";
            case "ran_tenjiku", "ran_rokuhara" -> "ran";
            case "rindo_tenjiku", "rindo_rokuhara" -> "rindo";
            case "mochizuki_tenjiku", "mochizuki_rokuhara" -> "kanji";
            default -> id;
        };
        this.texture = new ResourceLocation(TokyoRevengers.MOD_ID, "textures/entity/" + skinId + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(GangMemberEntity entity) {
        // Até existir uma skin dedicada, usa a textura genérica da gangue em vez do missing texture.
        if (Minecraft.getInstance().getResourceManager().getResource(texture).isPresent()) return texture;
        return new ResourceLocation(TokyoRevengers.MOD_ID, "textures/entity/toman_member.png");
    }
}
