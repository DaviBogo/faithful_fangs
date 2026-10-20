package com.davibogo.faithfulfangs.client.wolf;

import com.davibogo.faithfulfangs.wolf.WolfSleep;
import net.minecraft.client.model.animal.wolf.BabyWolfModel;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.animal.wolf.Wolf;

/**
 * Replaces the vanilla wolf renderer. It draws wolves exactly like vanilla (same code as
 * WolfRenderer), except that an adult wolf asleep in its Dog Bed is drawn curled up.
 * Puppies have no curled pose: they sleep sitting.
 */
public class CurlingWolfRenderer extends AgeableMobRenderer<Wolf, WolfRenderState, WolfModel> {
    public CurlingWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new CurlingAdultWolfModel(context.bakeLayer(ModelLayers.WOLF)),
                new BabyWolfModel(context.bakeLayer(ModelLayers.WOLF_BABY)), 0.5F);
        this.addLayer(new CurlingWolfArmorLayer(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new WolfCollarLayer(this));
    }

    @Override
    protected int getModelTint(WolfRenderState state) {
        float wetShade = state.wetShade;
        return wetShade == 1.0F ? -1 : ARGB.colorFromFloat(1.0F, wetShade, wetShade, wetShade);
    }

    @Override
    public Identifier getTextureLocation(WolfRenderState state) {
        return state.texture;
    }

    @Override
    public WolfRenderState createRenderState() {
        return new CurledWolfRenderState();
    }

    @Override
    public void extractRenderState(Wolf entity, WolfRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAngry = entity.isAngry();
        state.isSitting = entity.isInSittingPose();
        state.tailAngle = entity.getTailAngle();
        state.headRollAngle = entity.getHeadRollAngle(partialTicks);
        state.shakeAnim = entity.getShakeAnim(partialTicks);
        state.texture = entity.getTexture();
        state.wetShade = entity.getWetShade(partialTicks);
        state.collarColor = entity.isTame() ? entity.getCollarColor() : null;
        state.bodyArmorItem = entity.getBodyArmorItem().copy();

        boolean asleep = WolfSleep.isSleeping(entity);
        if (state instanceof CurledWolfRenderState curledState) {
            curledState.curled = asleep && !state.isBaby;
        }
        if (asleep && state.isBaby) {
            state.isSitting = true;
        }
    }
}
