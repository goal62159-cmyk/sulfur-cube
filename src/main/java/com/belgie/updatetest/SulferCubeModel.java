package com.belgie.updatetest;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SulferCubeModel extends EntityModel<SulferCubeRenderState> {
    public SulferCubeModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 50)
                        .addBox(-13.0f, -25.0f, -12.0f, 25.0f, 25.0f, 25.0f, new CubeDeformation(-1.0f)),
                PartPose.offset(0.0f, 24.0f, 0.0f));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public static LayerDefinition createOuterBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-13.0f, -25.0f, -12.0f, 25.0f, 25.0f, 25.0f, new CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 24.0f, 0.0f));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
