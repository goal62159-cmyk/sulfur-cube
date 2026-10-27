package com.belgie.updatetest;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class SulferCubeOuterLayer extends RenderLayer<SulferCubeRenderState, SulferCubeModel> {
    private final SulferCubeModel model;

    public SulferCubeOuterLayer(RenderLayerParent<SulferCubeRenderState, SulferCubeModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new SulferCubeModel(modelSet.bakeLayer(UpdateTestClient.SULFER_CUBE_MODEL_OUTER));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       SulferCubeRenderState state, float yRot, float xRot) {
        boolean glowingInvisible = state.appearsGlowing() && state.isInvisible;
        if (!state.isInvisible || glowingInvisible) {
            int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0f);
            if (glowingInvisible) {
                collector.order(1).submitModel(this.model, state, poseStack,
                        RenderType.outline(SulferCubeRenderer.TEXTURE), lightCoords, overlay, -1,
                        null, state.outlineColor, null);
            } else {
                collector.order(1).submitModel(this.model, state, poseStack,
                        RenderType.entityTranslucent(SulferCubeRenderer.TEXTURE), lightCoords, overlay, -1,
                        null, state.outlineColor, null);
            }
        }
    }
}
