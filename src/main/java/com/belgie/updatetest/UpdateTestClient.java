package com.belgie.updatetest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public class UpdateTestClient implements ClientModInitializer {
    public static final ModelLayerLocation SULFER_CUBE_MODEL = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(UpdateTest.MODID, "sulfer_sube"), "main");
    public static final ModelLayerLocation SULFER_CUBE_MODEL_OUTER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(UpdateTest.MODID, "sulfer_sube_outer"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(SULFER_CUBE_MODEL, SulferCubeModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SULFER_CUBE_MODEL_OUTER, SulferCubeModel::createOuterBodyLayer);
        EntityRendererRegistry.register(UpdateTest.SULFER_CUBE, SulferCubeRenderer::new);
    }
}
