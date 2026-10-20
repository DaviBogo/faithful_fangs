package com.davibogo.faithfulfangs.client.wolf;

import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.WolfRenderState;

/**
 * Vanilla adult wolf model with one extra pose: curled up asleep in its Dog Bed.
 * <p>
 * Seen from above (head side = bed opening): the chest lies on one side, the rear along the back
 * of the bed, the hind legs and tail come around the other side, and the head rests in front on
 * the folded paws, nose towards the tail. Positions are model pixels; y = 24 is the cushion top.
 * Every other pose is exactly vanilla.
 */
public class CurlingAdultWolfModel extends AdultWolfModel {
    private static final float HALF_PI = (float) (Math.PI / 2);
    private static final float DEG = (float) (Math.PI / 180);

    private final ModelPart upperBody;
    private final ModelPart realHead;
    private final ModelPart realTail;

    public CurlingAdultWolfModel(ModelPart root) {
        super(root);
        this.upperBody = root.getChild("upper_body");
        this.realHead = this.head.getChild("real_head");
        this.realTail = this.tail.getChild("real_tail");
    }

    @Override
    public void setupAnim(WolfRenderState state) {
        super.setupAnim(state);
        if (state instanceof CurledWolfRenderState curledState && curledState.curled) {
            this.curlUp();
        }
    }

    private void curlUp() {
        // Chest, lying on its side of the bed (sunk 1 px into the cushion).
        pose(this.upperBody, 3, 22, 0, HALF_PI, 90 * DEG, 0);
        // Rear, along the back of the bed.
        pose(this.body, 3, 22, 3, HALF_PI, -90 * DEG, 0);
        // Hind legs flat on the cushion, pointing forward on the other side.
        pose(this.rightHindLeg, -4, 23, 3, -HALF_PI, 0, 0);
        pose(this.leftHindLeg, -2, 23, 2, -HALF_PI, 0, 0);
        // Front legs folded under the head.
        pose(this.rightFrontLeg, 5, 23, -4, -HALF_PI, 90 * DEG, 0);
        pose(this.leftFrontLeg, 5, 23, -2, -HALF_PI, 90 * DEG, 0);
        // Head resting on the paws, turned towards the tail and slightly tilted.
        pose(this.head, 1.5F, 19.5F, -5.5F, 0, 70 * DEG, -20 * DEG);
        this.realHead.zRot = 0;
        // Tail wrapping around the front, towards the nose.
        pose(this.tail, -6, 23, 5, -HALF_PI, -15 * DEG, 0);
        this.realTail.zRot = 0;
    }

    private static void pose(ModelPart part, float x, float y, float z, float xRot, float yRot, float zRot) {
        part.x = x;
        part.y = y;
        part.z = z;
        part.xRot = xRot;
        part.yRot = yRot;
        part.zRot = zRot;
    }
}
