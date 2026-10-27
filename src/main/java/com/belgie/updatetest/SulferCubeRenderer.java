package com.belgie.updatetest;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class SulferCubeRenderer extends MobRenderer<SulferCubeEntity, SulferCubeRenderState, SulferCubeModel> {
    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(UpdateTest.MODID, "textures/entity/sulfer_cube.png");

    public SulferCubeRenderer(EntityRendererProvider.Context context) {
        super(context, new SulferCubeModel(context.bakeLayer(UpdateTestClient.SULFER_CUBE_MODEL)), 0.25f);
        this.addLayer(new SulferCubeBlockLayer(this));
        this.addLayer(new SulferCubeOuterLayer(this, context.getModelSet()));
    }

    @Override
    protected float getShadowRadius(SulferCubeRenderState state) {
        return (float) state.size * 0.25f;
    }

    @Override
    protected void scale(SulferCubeRenderState state, PoseStack poseStack) {
        poseStack.scale(0.25f, 0.25f, 0.25f);
        poseStack.translate(0.0f, 0.001f, 0.0f);
        float size = state.size;
        float ss = state.squish / (size * 0.5f + 1.0f);
        float w = 1.0f / (ss + 1.0f);
        poseStack.scale(w * size, 1.0f / w * size, w * size);
    }

    @Override
    public Identifier getTextureLocation(SulferCubeRenderState state) {
        return TEXTURE;
    }

    @Override
    public SulferCubeRenderState createRenderState() {
        return new SulferCubeRenderState();
    }

    @Override
    public void extractRenderState(SulferCubeEntity entity, SulferCubeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.squish = Mth.lerp(partialTicks, entity.oSquish, entity.squish);
        state.size = entity.getSize();
        state.carriedBlock = entity.getCarriedBlock();
    }
}
