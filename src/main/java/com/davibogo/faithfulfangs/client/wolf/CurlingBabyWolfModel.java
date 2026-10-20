package com.davibogo.faithfulfangs.client.wolf;

import net.minecraft.client.model.animal.wolf.BabyWolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.WolfRenderState;

/**
 * Vanilla puppy model with one extra pose: lying asleep in its Dog Bed.
 * <p>
 * Seen from above (head side = bed opening): the head rests on the cushion facing the opening,
 * the body lies diagonally behind it, the front paws lie beside the head (never across the face),
 * one hind leg is tucked under the body and the tail curls forward along the other side. Positions
 * are model pixels; y = 24 is the cushion top. Every other pose is exactly vanilla.
 */
public class CurlingBabyWolfModel extends BabyWolfModel {
    private static final float HALF_PI = (float) (Math.PI / 2);
    private static final float DEG = (float) (Math.PI / 180);
    /** The tail's child part is tilted by -3.1 rad in the vanilla model; this lays the whole tail flat. */
    private static final float TAIL_FLAT = HALF_PI + 3.1F;

    public CurlingBabyWolfModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(WolfRenderState state) {
        super.setupAnim(state);
        if (state instanceof CurledWolfRenderState curledState && curledState.curled) {
            this.curlUp();
        }
    }

    private void curlUp() {
        CurlingAdultWolfModel.pose(this.body, -0.4F, 22.25F, 1.1F, 0, -60 * DEG, 0);
        CurlingAdultWolfModel.pose(this.head, 2, 22.25F, -2.7F, 0, 0, -15 * DEG);
        CurlingAdultWolfModel.pose(this.rightFrontLeg, 2.2F, 23, 2.7F, -HALF_PI, -90 * DEG, 0);
        CurlingAdultWolfModel.pose(this.leftFrontLeg, 2.2F, 23, 0.65F, -HALF_PI, -90 * DEG, 0);
        CurlingAdultWolfModel.pose(this.rightHindLeg, -1.9F, 23, 4.5F, -HALF_PI, -50 * DEG, 0);
        CurlingAdultWolfModel.pose(this.leftHindLeg, -2.5F, 23, 0.5F, -HALF_PI, -90 * DEG, 0);
        CurlingAdultWolfModel.pose(this.tail, -3.5F, 22.5F, 2.8F, TAIL_FLAT, 10 * DEG, 0);
    }
}
