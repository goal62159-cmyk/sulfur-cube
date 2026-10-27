package com.belgie.updatetest;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class SulferCubeRenderState extends LivingEntityRenderState {
    public float squish;
    public int size = 1;
    public @Nullable BlockState carriedBlock;
}
