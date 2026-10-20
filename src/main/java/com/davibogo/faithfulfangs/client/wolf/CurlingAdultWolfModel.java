package com.davibogo.faithfulfangs.client.wolf;

import net.minecraft.client.model.animal.wolf.AdultWolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.WolfRenderState;

/**
 * Vanilla adult wolf model with one extra pose: curled up asleep in its Dog Bed.
 * <p>
 * Seen from above (head side = bed opening): the chest lies in front with the neck towards the
 * opening, the rear goes diagonally to the back corner, the hind legs and tail come forward along
 * the other side, and the head rests on the front paws, turned towards them. The head keeps its
 * vanilla place on the neck, so the collar stays around the neck. Positions are model pixels;
 * y = 24 is the cushion top. Every other pose is exactly vanilla.
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
        // Chest, neck towards the bed opening (sunk 1 px into the cushion).
        pose(this.upperBody, 0.75F, 22, -1.75F, HALF_PI, 0, 0);
        // Rear, diagonally towards the back corner. Its front end stays inside the chest: vanilla
        // leaves that side of the body untextured because it is always hidden there.
        pose(this.body, 1.6F, 22, -0.6F, HALF_PI, -55 * DEG, 0);
        // Hind legs folded forward along the other side.
        pose(this.rightHindLeg, -4, 23, 2, -HALF_PI, 0, 0);
        pose(this.leftHindLeg, -2, 23, 1, -HALF_PI, 0, 0);
        // Front legs under the chin, pointing towards the hind legs.
        pose(this.rightFrontLeg, 3, 23, -4.5F, -HALF_PI, 90 * DEG, 0);
        pose(this.leftFrontLeg, 3, 23, -6.5F, -HALF_PI, 90 * DEG, 0);
        // Head in its vanilla place in front of the neck, resting on the paws, turned towards the
        // hind legs and slightly tilted.
        pose(this.head, 0.75F, 19.5F, -5.75F, 0, 80 * DEG, -20 * DEG);
        this.realHead.zRot = 0;
        // Tail over the hind legs, tip towards the nose.
        pose(this.tail, -5.5F, 21.5F, 4, -HALF_PI, -10.6F * DEG, 0);
        this.realTail.zRot = 0;
    }

    static void pose(ModelPart part, float x, float y, float z, float xRot, float yRot, float zRot) {
        part.x = x;
        part.y = y;
        part.z = z;
        part.xRot = xRot;
        part.yRot = yRot;
        part.zRot = zRot;
    }
}
