package com.davibogo.faithfulfangs.client;

import com.davibogo.faithfulfangs.block.DogBedBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Shows the bound wolf's name above a Dog Bed while the player looks at it, the same way
 * vanilla shows a mob's Name Tag. The bed itself is drawn by its block model.
 */
public class DogBedRenderer implements BlockEntityRenderer<DogBedBlockEntity, DogBedRenderer.State> {
    /** Where the label is anchored, relative to the bed's block corner (vanilla adds 0.5 above it). */
    private static final Vec3 NAME_ATTACHMENT = new Vec3(0.5, 0.45, 0.5);

    public DogBedRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static class State extends BlockEntityRenderState {
        public @Nullable Component name;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DogBedBlockEntity bed, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(bed, state, partialTicks, cameraPosition, breakProgress);
        state.name = bed.isBound() && isLookedAt(bed) ? Component.literal(bed.getWolfName()) : null;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.name != null) {
            collector.submitNameTag(poseStack, NAME_ATTACHMENT, 0, state.name, true, state.lightCoords, camera);
        }
    }

    /** True if the crosshair is on this bed (only happens within reach, like vanilla mob names). */
    private static boolean isLookedAt(DogBedBlockEntity bed) {
        HitResult hit = Minecraft.getInstance().hitResult;
        return hit != null && hit.getType() == HitResult.Type.BLOCK
                && hit instanceof BlockHitResult blockHit
                && blockHit.getBlockPos().equals(bed.getBlockPos());
    }
}
