package com.belgie.updatetest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Cube ke andar jo block "carry" ho raha hai usse draw karta hai. */
public class SulferCubeBlockLayer extends RenderLayer<SulferCubeRenderState, SulferCubeModel> {
    public SulferCubeBlockLayer(RenderLayerParent<SulferCubeRenderState, SulferCubeModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       SulferCubeRenderState state, float yRot, float xRot) {
        if (state.carriedBlock != null && !state.carriedBlock.isAir()) {
            poseStack.pushPose();
            poseStack.translate(0.72f, 1.47f, -0.72f);
            poseStack.scale(1.5f, 1.5f, 1.5f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            collector.submitBlock(poseStack, state.carriedBlock, lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
