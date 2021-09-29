package com.majruszs_difficulty.renderers;

import com.majruszs_difficulty.MajruszsDifficulty;
import com.majruszs_difficulty.entities.TankEntity;
import com.majruszs_difficulty.models.TankModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn( Dist.CLIENT )
public class TankRenderer extends MobRenderer< TankEntity, TankModel< TankEntity > > {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation( MajruszsDifficulty.getLocation( "tank" ), "main" );
	private static final ResourceLocation TEXTURE_LOCATION = MajruszsDifficulty.getLocation( "textures/entity/tank.png" );

	public TankRenderer( EntityRendererProvider.Context context ) {
		super( context, new TankModel<>( context.bakeLayer( LAYER_LOCATION ) ), 0.5f );
	}

	@Override
	public ResourceLocation getTextureLocation( TankEntity tank ) {
		return TEXTURE_LOCATION;
	}

	@Override
	public void render( TankEntity tank, float p_114209_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight ) {
		this.model.prepareMobModel( tank, 0.0F, 0.0F, partialTicks );

		super.render( tank, p_114209_, partialTicks, poseStack, bufferSource, packedLight );
	}
}